package com.kosi.financetracker.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "budgets")
public class Budget {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String category;
    
    @Column(nullable = false)
    private Double limitAmount;
    
    // Remove spent - it will be calculated from transactions
    @Transient
    private Double spent = 0.0; // Transient means not stored in DB
    
    private String color;
    
    @Column(name = "budget_month", nullable = false)
    private Integer month;
    
    @Column(name = "budget_year", nullable = false)
    private Integer year;
    
    @Column(nullable = false)
    private String currency = "USD";
    
    @Column(updatable = false)
    private LocalDate createdAt;
    
    private LocalDate updatedAt;
    
    // Constructors
    public Budget() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
        this.month = LocalDate.now().getMonthValue();
        this.year = LocalDate.now().getYear();
    }
    
    public Budget(Long userId, String category, Double limitAmount, Integer month, Integer year) {
        this();
        this.userId = userId;
        this.category = category;
        this.limitAmount = limitAmount;
        this.month = month;
        this.year = year;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
        updatedAt = LocalDate.now();
        if (month == null) {
            month = LocalDate.now().getMonthValue();
        }
        if (year == null) {
            year = LocalDate.now().getYear();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Double getLimitAmount() { return limitAmount; }
    public void setLimitAmount(Double limitAmount) { this.limitAmount = limitAmount; }
    
    public Double getSpent() { return spent != null ? spent : 0.0; }
    public void setSpent(Double spent) { this.spent = spent; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
    
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
    
    public LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDate updatedAt) { this.updatedAt = updatedAt; }
    
    // Utility methods
    public double getPercentageUsed() {
        if (limitAmount <= 0) return 0;
        return (spent / limitAmount) * 100;
    }
    
    public boolean isOverBudget() {
        return spent > limitAmount;
    }
    
    public boolean isNearLimit() {
        return spent > (limitAmount * 0.8) && spent <= limitAmount;
    }
}