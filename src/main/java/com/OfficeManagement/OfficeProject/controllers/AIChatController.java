package com.OfficeManagement.OfficeProject.controllers;

import com.OfficeManagement.OfficeProject.dtos.UserContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "http://localhost:3000")
public class AIChatController {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate;

    public AIChatController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ==================== MAIN CHAT ENDPOINT ====================

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chatWithAI(@RequestBody Map<String, String> request) {
        try {
            String userMessage = request.get("message");
            if (userMessage == null || userMessage.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Message cannot be empty");
                return ResponseEntity.badRequest().body(error);
            }

            String role = UserContext.getCurrentRole();
            String username = UserContext.getCurrentUsername();

            // Get current date to prevent backdated responses
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
            String currentDateTime = java.time.LocalDateTime.now().toString();

            // Check if this is a date-related query
            if (isDateRelatedQuery(userMessage)) {
                return handleDateQueryDirectly(userMessage, role, username, currentDate, currentDateTime);
            }

            String roleContext = getRoleContext(role);

            // Enhanced prompt with current date context
            String enhancedPrompt = "CURRENT DATE CONTEXT: Today is " + currentDate +
                    " (Timestamp: " + currentDateTime + "). " +
                    "Always use this current date in your responses.\n\n" +
                    roleContext + "\n\n" +
                    "User Question: " + userMessage;

            System.out.println("=== AI REQUEST ===");
            System.out.println("User: " + username + ", Role: " + role);
            System.out.println("Current Date: " + currentDate);
            System.out.println("Prompt: " + enhancedPrompt);

            String aiResponse = callGeminiAPI(enhancedPrompt);

            System.out.println("=== AI RESPONSE ===");
            System.out.println("Source: " + (aiResponse.contains("🔄 FALLBACK") ? "FALLBACK" : "GEMINI"));
            System.out.println(aiResponse);

            Map<String, Object> response = new HashMap<>();
            response.put("response", aiResponse);
            response.put("role", role);
            response.put("username", username);
            response.put("status", "success");
            response.put("timestamp", new Date());
            response.put("source", aiResponse.contains("🔄 FALLBACK") ? "fallback" : "gemini");
            response.put("currentSystemDate", currentDate); // Include actual system date

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("=== AI ERROR ===");
            e.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("error", "AI Service Error: " + e.getMessage());
            error.put("timestamp", new Date());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ==================== UPDATED GEMINI API CALL ====================

    private String callGeminiAPI(String prompt) {
        System.out.println("🎯 USING AVAILABLE GEMINI MODELS");

        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            System.out.println("❌ NO API KEY CONFIGURED");
            return "🔄 FALLBACK: " + getEnhancedFallbackResponse(prompt);
        }

        // ✅ ACTUALLY AVAILABLE GEMINI ENDPOINTS:
        String[] modelEndpoints = {
                // Gemini 1.5 Flash (Latest & Fastest - RECOMMENDED)
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent",
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent",

                // Gemini 1.5 Pro (More Capable)
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent",
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro-latest:generateContent",

                // Original Gemini Pro (Most Stable)
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent",
                "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent"
        };

        for (String endpoint : modelEndpoints) {
            String apiUrl = endpoint + "?key=" + geminiApiKey;
            System.out.println("🔄 Trying: " + endpoint);

            try {
                Map<String, Object> requestBody = createGeminiRequestBody(prompt);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

                ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);

                System.out.println("📡 Response Status: " + response.getStatusCode());

                if (response.getStatusCode() == HttpStatus.OK) {
                    System.out.println("✅ SUCCESS with: " + endpoint);
                    String result = parseGeminiResponse(response.getBody());
                    return result;
                } else {
                    System.out.println("❌ Failed with status: " + response.getStatusCode());
                }

            } catch (Exception e) {
                System.out.println("❌ FAILED: " + endpoint + " - " + e.getMessage());
                // Continue to next endpoint
            }
        }

        System.out.println("❌ ALL GEMINI MODELS FAILED");
        return getEnhancedFallbackResponse(prompt);
    }

    // ==================== IMPROVED REQUEST BODY ====================

    private Map<String, Object> createGeminiRequestBody(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();

        // Create contents array
        List<Map<String, Object>> contentsList = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();

        // Create parts array
        List<Map<String, String>> partsList = new ArrayList<>();
        Map<String, String> textPart = new HashMap<>();
        textPart.put("text", prompt);
        partsList.add(textPart);

        content.put("parts", partsList);
        contentsList.add(content);
        requestBody.put("contents", contentsList);

        // Improved generation config
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("topK", 40);
        generationConfig.put("topP", 0.95);
        generationConfig.put("maxOutputTokens", 1024);
        generationConfig.put("responseMimeType", "text/plain");
        requestBody.put("generationConfig", generationConfig);

        // Add safety settings
        List<Map<String, String>> safetySettings = new ArrayList<>();
        String[] categories = {"HARM_CATEGORY_HARASSMENT", "HARM_CATEGORY_HATE_SPEECH",
                "HARM_CATEGORY_SEXUALLY_EXPLICIT", "HARM_CATEGORY_DANGEROUS_CONTENT"};

        for (String category : categories) {
            Map<String, String> setting = new HashMap<>();
            setting.put("category", category);
            setting.put("threshold", "BLOCK_MEDIUM_AND_ABOVE");
            safetySettings.add(setting);
        }
        requestBody.put("safetySettings", safetySettings);

        return requestBody;
    }

    // ==================== DATE QUERY HANDLING ====================

    private boolean isDateRelatedQuery(String message) {
        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("today") ||
                lowerMessage.contains("date") ||
                lowerMessage.contains("time") ||
                lowerMessage.contains("current date") ||
                lowerMessage.contains("what day") ||
                lowerMessage.contains("what is the date") ||
                lowerMessage.contains("current time");
    }

    private ResponseEntity<Map<String, Object>> handleDateQueryDirectly(String userMessage, String role,
                                                                        String username, String currentDate,
                                                                        String currentDateTime) {
        String responseText;
        String currentTime = java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        String lowerMessage = userMessage.toLowerCase();

        if (lowerMessage.contains("today") && lowerMessage.contains("date")) {
            responseText = "Today's date is **" + currentDate + "**.";
            if (lowerMessage.contains("time")) {
                responseText += " The current time is **" + currentTime + "**.";
            }
        } else if (lowerMessage.contains("time")) {
            responseText = "The current time is **" + currentTime + "**.";
            if (lowerMessage.contains("date")) {
                responseText += " Today's date is **" + currentDate + "**.";
            }
        } else if (lowerMessage.contains("what day") || lowerMessage.contains("what is the date")) {
            responseText = "Today is **" + currentDate + "**.";
        } else {
            // For complex date queries, use AI but with strong context
            String enhancedPrompt = "CRITICAL: Today is " + currentDate + ", time is " + currentTime +
                    ". You MUST use these current values.\n\nQuestion: " + userMessage;
            responseText = callGeminiAPI(enhancedPrompt);
        }

        // Add role-specific context
        responseText = addRoleContextToResponse(responseText, role, username);

        Map<String, Object> response = new HashMap<>();
        response.put("response", responseText);
        response.put("role", role);
        response.put("username", username);
        response.put("status", "success");
        response.put("timestamp", new Date());
        response.put("source", "system_date");
        response.put("currentSystemDate", currentDate);
        response.put("currentSystemTime", currentTime);

        return ResponseEntity.ok(response);
    }

    private String addRoleContextToResponse(String response, String role, String username) {
        Map<String, String> rolePrefixes = new HashMap<>();
        rolePrefixes.put("ROLE_HR", "As HR Manager " + username + ": ");
        rolePrefixes.put("ROLE_DIRECTOR", "As Director " + username + ": ");
        rolePrefixes.put("ROLE_PROJECT_MANAGER", "As Project Manager " + username + ": ");
        rolePrefixes.put("ROLE_CTO", "As CTO " + username + ": ");
        rolePrefixes.put("ROLE_USER", "Hello " + username + "! ");

        String prefix = rolePrefixes.getOrDefault(role, "");
        return prefix + response;
    }

    // ==================== RESPONSE PARSING ====================

    private String parseGeminiResponse(Map<String, Object> response) {
        try {
            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    if (candidate.containsKey("content")) {
                        Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                        if (content.containsKey("parts")) {
                            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                            if (parts != null && !parts.isEmpty()) {
                                Object text = parts.get(0).get("text");
                                return text != null ? text.toString().trim() : "No text in response";
                            }
                        }
                    }
                }
            }
            return "No valid response from AI service";
        } catch (Exception e) {
            System.out.println("Error parsing response: " + e.getMessage());
            return "Error parsing AI response: " + e.getMessage();
        }
    }

    // ==================== FALLBACK RESPONSE ====================

    private String getEnhancedFallbackResponse(String prompt) {
        String role = UserContext.getCurrentRole();
        String username = UserContext.getCurrentUsername();
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));

        Map<String, String> roleResponses = new HashMap<>();
        roleResponses.put("ROLE_HR",
                "As HR Manager " + username + ", based on your question:\n\n" +
                        "I understand you're asking: \"" + extractUserQuestion(prompt) + "\"\n\n" +
                        "📊 **HR Perspective**: Focus on employee engagement and development.\n" +
                        "📅 **Current Date**: " + currentDate + "\n" +
                        "💡 **Note**: AI service is currently unavailable, but here's general advice...");

        roleResponses.put("ROLE_PROJECT_MANAGER",
                "As Project Manager " + username + ", regarding your query:\n\n" +
                        "Your question: \"" + extractUserQuestion(prompt) + "\"\n\n" +
                        "🔄 **Project Focus**: Timeline and resource management.\n" +
                        "📅 **Current Date**: " + currentDate + "\n" +
                        "💡 **Note**: Using fallback response - AI service unavailable.");

        roleResponses.put("ROLE_DIRECTOR",
                "As Director " + username + ", strategic viewpoint:\n\n" +
                        "Regarding: \"" + extractUserQuestion(prompt) + "\"\n\n" +
                        "🎯 **Strategic Alignment**: Business objectives and growth.\n" +
                        "📅 **Current Date**: " + currentDate + "\n" +
                        "💡 **Note**: Fallback response - AI service temporarily down.");

        roleResponses.put("ROLE_CTO",
                "As CTO " + username + ", technical perspective:\n\n" +
                        "Your query: \"" + extractUserQuestion(prompt) + "\"\n\n" +
                        "⚡ **Technology Focus**: Infrastructure and innovation.\n" +
                        "📅 **Current Date**: " + currentDate + "\n" +
                        "💡 **Note**: AI service unavailable - using fallback.");

        roleResponses.put("ROLE_USER",
                "Hello " + username + "! Regarding your question:\n\n" +
                        "\"" + extractUserQuestion(prompt) + "\"\n\n" +
                        "As an employee, focus on task completion and collaboration.\n" +
                        "📅 **Current Date**: " + currentDate + "\n" +
                        "💡 **Note**: AI service is currently down, but here's general guidance.");

        return roleResponses.getOrDefault(role,
                "Hello " + username + "! I understand you're asking: \"" + extractUserQuestion(prompt) + "\"\n\n" +
                        "📅 **Current Date**: " + currentDate + "\n" +
                        "💡 **Note**: AI service is temporarily unavailable. For assistance, contact support.");
    }

    private String extractUserQuestion(String prompt) {
        // Extract the original user question from the enhanced prompt
        if (prompt.contains("User Question:")) {
            return prompt.substring(prompt.indexOf("User Question:") + "User Question:".length()).trim();
        }
        return prompt;
    }

    // ==================== ROLE CONTEXT ====================

    private String getRoleContext(String role) {
        Map<String, String> roleContexts = new HashMap<>();
        roleContexts.put("ROLE_HR", "You are an expert HR Manager AI assistant. Provide HR-focused responses about employee management, policies, and workplace culture.");
        roleContexts.put("ROLE_DIRECTOR", "You are a strategic Director AI assistant. Focus on business strategy, organizational goals, and leadership perspectives.");
        roleContexts.put("ROLE_PROJECT_MANAGER", "You are a skilled Project Manager AI assistant. Emphasize project planning, team coordination, and delivery timelines.");
        roleContexts.put("ROLE_CTO", "You are a technical CTO AI assistant. Provide insights on technology strategy, infrastructure, and innovation.");
        return roleContexts.getOrDefault(role, "You are a helpful AI assistant.");
    }

    // ==================== DEBUG & TEST ENDPOINTS ====================

    @GetMapping("/debug-key")
    public ResponseEntity<Map<String, Object>> debugApiKey() {
        Map<String, Object> response = new HashMap<>();
        response.put("apiKeyExists", geminiApiKey != null && !geminiApiKey.trim().isEmpty());
        response.put("apiKeyLength", geminiApiKey != null ? geminiApiKey.length() : 0);
        response.put("apiKeyPreview", geminiApiKey != null ?
                geminiApiKey.substring(0, Math.min(10, geminiApiKey.length())) + "..." : "null");
        response.put("timestamp", new Date());
        response.put("currentSystemDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        response.put("status", "debug_endpoint_working");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/test-gemini")
    public ResponseEntity<Map<String, Object>> testGeminiDirect() {
        Map<String, Object> result = new HashMap<>();

        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            result.put("status", "ERROR");
            result.put("message", "API key is not configured");
            return ResponseEntity.badRequest().body(result);
        }

        // Test available models
        String[] testEndpoints = {
                "v1beta/models/gemini-1.5-flash",
                "v1beta/models/gemini-1.5-flash-latest",
                "v1beta/models/gemini-1.5-pro",
                "v1beta/models/gemini-pro",
                "v1/models/gemini-pro"
        };

        List<Map<String, Object>> testResults = new ArrayList<>();

        for (String endpoint : testEndpoints) {
            Map<String, Object> testResult = new HashMap<>();
            testResult.put("endpoint", endpoint);

            try {
                String apiUrl = "https://generativelanguage.googleapis.com/" + endpoint + ":generateContent?key=" + geminiApiKey;
                String testPrompt = "Hello, respond with just 'AI_IS_WORKING' and today's actual date.";

                Map<String, Object> requestBody = createGeminiRequestBody(testPrompt);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

                ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);

                testResult.put("status", response.getStatusCode().toString());
                testResult.put("success", response.getStatusCode() == HttpStatus.OK);

                if (response.getStatusCode() == HttpStatus.OK) {
                    String aiResponse = parseGeminiResponse(response.getBody());
                    testResult.put("response", aiResponse);
                    testResult.put("working", aiResponse.contains("AI_IS_WORKING"));
                    testResult.put("containsDate", !aiResponse.contains("June 11, 2024")); // Check for backdated response
                }

            } catch (Exception e) {
                testResult.put("status", "ERROR");
                testResult.put("error", e.getMessage());
            }

            testResults.add(testResult);
        }

        result.put("testResults", testResults);
        result.put("apiKeyConfigured", true);
        result.put("apiKeyLength", geminiApiKey.length());
        result.put("currentSystemDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));

        // Check if any model worked
        boolean anyWorking = testResults.stream()
                .anyMatch(r -> Boolean.TRUE.equals(r.get("working")));

        result.put("overallStatus", anyWorking ? "AT_LEAST_ONE_MODEL_WORKING" : "ALL_MODELS_FAILED");

        return ResponseEntity.ok(result);
    }

    @GetMapping("/system-status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();

        status.put("service", "AI Chat Controller");
        status.put("status", "RUNNING");
        status.put("timestamp", new Date());
        status.put("currentDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        status.put("currentTime", java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        status.put("javaVersion", System.getProperty("java.version"));
        status.put("apiKeyConfigured", geminiApiKey != null && !geminiApiKey.trim().isEmpty());

        return ResponseEntity.ok(status);
    }
}