package com.OfficeManagement.OfficeProject.dtos;

public class UserContext {
    private static final ThreadLocal<String> currentRole = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUsername = new ThreadLocal<>();

    public static String getCurrentRole() {
        return currentRole.get();
    }

    public static void setCurrentRole(String role) {
        currentRole.set(role);
    }

    public static String getCurrentUsername() {
        return currentUsername.get();
    }

    public static void setCurrentUsername(String username) {
        currentUsername.set(username);
    }

    // Optional: Helper method to set both at once
    public static void setCurrentUser(String username, String role) {
        setCurrentUsername(username);
        setCurrentRole(role);
    }

    public static void clear() {
        currentRole.remove();
        currentUsername.remove();
    }
}