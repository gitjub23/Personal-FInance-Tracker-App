package com.kosi.financetracker.backend.Controller;

import com.kosi.financetracker.backend.model.User;
import com.kosi.financetracker.backend.service.EmailService;
import com.kosi.financetracker.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService; // INJECTED EMAIL SERVICE

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationRequest request) {
        try {
            if (userService.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(createErrorResponse("Email already exists"));
            }

            // 1. Create the user
            User user = userService.createUser(request);

            // 2. SEND VERIFICATION EMAIL
            // This generates the code and sends it via SMTP
            try {
                emailService.sendVerificationOTP(user, "registration");
            } catch (Exception e) {
                // If email fails, we log it but don't fail the whole registration
                // In production, you might want to handle this differently
                System.err.println("Failed to send email: " + e.getMessage());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully. Please check your email for verification code.");
            response.put("userId", user.getId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("token", "dummy-token");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        try {
            User user = userService.authenticateUser(request.getEmail(), request.getPassword());

            if (user == null) {
                return ResponseEntity.status(401).body(createErrorResponse("Invalid email or password"));
            }

            user.setLastLogin(LocalDateTime.now());
            userService.updateUser(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("token", "dummy-token");
            response.put("userId", user.getId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(401).body(createErrorResponse("Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            boolean success = userService.changePassword(
                request.getUserId(), 
                request.getCurrentPassword(), 
                request.getNewPassword()
            );

            if (success) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Password changed successfully");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("Current password is incorrect"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to change password: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete-account/{userId}")
    public ResponseEntity<?> deleteAccount(@PathVariable Long userId, @RequestBody DeleteAccountRequest request) {
        try {
            boolean success = userService.deleteAccount(userId, request.getPassword());

            if (success) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Account deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("Password is incorrect"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to delete account: " + e.getMessage()));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        return errorResponse;
    }

    public static class UserRegistrationRequest {
        private String name;
        private String email;
        private String password;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class ChangePasswordRequest {
        private Long userId;
        private String currentPassword;
        private String newPassword;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class DeleteAccountRequest {
        private String password;

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}