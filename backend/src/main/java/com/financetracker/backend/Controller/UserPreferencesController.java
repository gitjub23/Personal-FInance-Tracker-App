package com.kosi.financetracker.backend.Controller;

import com.kosi.financetracker.backend.model.UserPreferences;
import com.kosi.financetracker.backend.repository.UserPreferencesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/preferences")
@CrossOrigin(origins = "http://localhost:3000")
public class UserPreferencesController {
    
    @Autowired
    private UserPreferencesRepository preferencesRepository;
    
    // TEST ENDPOINT - Add this temporarily for debugging
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("message", "Preferences API is working");
            response.put("totalPreferences", preferencesRepository.count());
            response.put("allPreferences", preferencesRepository.findAll());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("stackTrace", e.getStackTrace());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserPreferences(@PathVariable Long userId) {
        try {
            System.out.println("=== GET Preferences for userId: " + userId + " ===");
            
            return preferencesRepository.findByUserId(userId)
                .map(prefs -> {
                    System.out.println("Found existing preferences: " + prefs);
                    return ResponseEntity.ok(prefs);
                })
                .orElseGet(() -> {
                    System.out.println("No preferences found, creating defaults");
                    // Return default preferences if none exist
                    UserPreferences defaultPrefs = new UserPreferences();
                    defaultPrefs.setUserId(userId);
                    defaultPrefs.setCurrency("USD");
                    defaultPrefs.setBudgetAlerts(true);
                    defaultPrefs.setWeeklyReports(true);
                    defaultPrefs.setReminders(false);
                    
                    try {
                        // Save default preferences
                        UserPreferences saved = preferencesRepository.save(defaultPrefs);
                        System.out.println("Saved default preferences: " + saved);
                        return ResponseEntity.ok(saved);
                    } catch (Exception e) {
                        System.err.println("Error saving default preferences: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });
        } catch (Exception e) {
            System.err.println("=== Error in GET Preferences ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to load preferences: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/user/{userId}")
    public ResponseEntity<?> saveUserPreferences(
            @PathVariable Long userId,
            @RequestBody UserPreferences preferences) {
        try {
            System.out.println("=== POST Preferences for userId: " + userId + " ===");
            System.out.println("Received preferences: " + preferences);
            
            UserPreferences toSave = preferencesRepository.findByUserId(userId)
                .orElse(new UserPreferences());
            
            System.out.println("Existing preferences: " + toSave);
            
            toSave.setUserId(userId);
            toSave.setCurrency(preferences.getCurrency() != null ? preferences.getCurrency() : "USD");
            toSave.setBudgetAlerts(preferences.getBudgetAlerts() != null ? preferences.getBudgetAlerts() : true);
            toSave.setWeeklyReports(preferences.getWeeklyReports() != null ? preferences.getWeeklyReports() : true);
            toSave.setReminders(preferences.getReminders() != null ? preferences.getReminders() : false);
            
            System.out.println("About to save: " + toSave);
            
            UserPreferences saved = preferencesRepository.save(toSave);
            
            System.out.println("Successfully saved: " + saved);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            System.err.println("=== Error in POST Preferences ===");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error class: " + e.getClass().getName());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to save preferences: " + e.getMessage());
            error.put("errorType", e.getClass().getName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/user/{userId}")
    public ResponseEntity<?> updateUserPreferences(
            @PathVariable Long userId,
            @RequestBody UserPreferences preferences) {
        try {
            System.out.println("=== PUT Preferences for userId: " + userId + " ===");
            System.out.println("Received preferences: " + preferences);
            
            UserPreferences toUpdate = preferencesRepository.findByUserId(userId)
                .orElse(new UserPreferences());
            
            System.out.println("Existing preferences: " + toUpdate);
            
            toUpdate.setUserId(userId);
            
            // Only update fields that are provided
            if (preferences.getCurrency() != null) {
                System.out.println("Updating currency to: " + preferences.getCurrency());
                toUpdate.setCurrency(preferences.getCurrency());
            }
            if (preferences.getBudgetAlerts() != null) {
                System.out.println("Updating budgetAlerts to: " + preferences.getBudgetAlerts());
                toUpdate.setBudgetAlerts(preferences.getBudgetAlerts());
            }
            if (preferences.getWeeklyReports() != null) {
                System.out.println("Updating weeklyReports to: " + preferences.getWeeklyReports());
                toUpdate.setWeeklyReports(preferences.getWeeklyReports());
            }
            if (preferences.getReminders() != null) {
                System.out.println("Updating reminders to: " + preferences.getReminders());
                toUpdate.setReminders(preferences.getReminders());
            }
            
            System.out.println("About to save: " + toUpdate);
            
            UserPreferences updated = preferencesRepository.save(toUpdate);
            
            System.out.println("Successfully updated: " + updated);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            System.err.println("=== Error in PUT Preferences ===");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error class: " + e.getClass().getName());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update preferences: " + e.getMessage());
            error.put("errorType", e.getClass().getName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}