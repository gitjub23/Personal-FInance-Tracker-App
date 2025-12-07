package com.kosi.financetracker.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_preferences")
public class UserPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private Long userId;
    
    @Column(nullable = false)
    private String currency = "USD";
    
    private Boolean budgetAlerts = true;
    
    private Boolean weeklyReports = true;
    
    private Boolean reminders = false;
    
    @Column(updatable = false)
    private java.time.LocalDateTime createdAt;
    
    private java.time.LocalDateTime updatedAt;
    
    // Constructors
    public UserPreferences() {
    }
    
    public UserPreferences(Long userId, String currency, Boolean budgetAlerts, 
                          Boolean weeklyReports, Boolean reminders) {
        this.userId = userId;
        this.currency = currency;
        this.budgetAlerts = budgetAlerts;
        this.weeklyReports = weeklyReports;
        this.reminders = reminders;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public Boolean getBudgetAlerts() {
        return budgetAlerts;
    }
    
    public void setBudgetAlerts(Boolean budgetAlerts) {
        this.budgetAlerts = budgetAlerts;
    }
    
    public Boolean getWeeklyReports() {
        return weeklyReports;
    }
    
    public void setWeeklyReports(Boolean weeklyReports) {
        this.weeklyReports = weeklyReports;
    }
    
    public Boolean getReminders() {
        return reminders;
    }
    
    public void setReminders(Boolean reminders) {
        this.reminders = reminders;
    }
    
    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}