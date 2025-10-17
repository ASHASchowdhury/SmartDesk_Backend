// src/main/java/com/OfficeManagement/OfficeProject/dtos/UserContext.java
package com.OfficeManagement.OfficeProject.dtos;

public class UserContext {
    private static final ThreadLocal<String> currentUsername = new ThreadLocal<>();
    private static final ThreadLocal<String> currentRole = new ThreadLocal<>();

    public static void setCurrentUser(String username, String role) {
        currentUsername.set(username);
        currentRole.set(role);
    }

    public static String getCurrentUsername() {
        return currentUsername.get();
    }

    public static String getCurrentRole() {
        return currentRole.get();
    }

    public static void clear() {
        currentUsername.remove();
        currentRole.remove();
    }
}
