package com.OfficeManagement.OfficeProject.controllers;

import com.OfficeManagement.OfficeProject.services.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "http://localhost:3000")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    public FileUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"error\": \"File is empty\"}");
            }

            // Check file size (max 10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("{\"error\": \"File size too large. Maximum 10MB allowed.\"}");
            }

            String fileName = fileStorageService.storeFile(file);

            FileUploadResponse response = new FileUploadResponse();
            response.setFileName(fileName);
            response.setFileDownloadUri("/api/files/download/" + fileName);
            response.setSize(file.getSize());
            response.setFileType(file.getContentType());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"Failed to upload file: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            if (fileName == null || fileName.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            Path filePath = fileStorageService.loadFile(fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Determine content type
                String contentType = "application/octet-stream";
                if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (fileName.toLowerCase().endsWith(".png")) {
                    contentType = "image/png";
                } else if (fileName.toLowerCase().endsWith(".gif")) {
                    contentType = "image/gif";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    public static class FileUploadResponse {
        private String fileName;
        private String fileDownloadUri;
        private String fileType;
        private long size;

        // Getters and setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getFileDownloadUri() { return fileDownloadUri; }
        public void setFileDownloadUri(String fileDownloadUri) { this.fileDownloadUri = fileDownloadUri; }

        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }

        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
    }
}