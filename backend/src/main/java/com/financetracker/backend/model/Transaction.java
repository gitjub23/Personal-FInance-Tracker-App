package com.kosi.financetracker.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String type; // "income" or "expense"

    private String category;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Long userId;
    
    // Store the currency the transaction was made in
    @Column(nullable = false)
    private String currency = "USD";

    // Constructors
    public Transaction() {}

    public Transaction(String name, Double amount, String type, String category, LocalDate date, Long userId, String currency) {
        this.name = name;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
        this.userId = userId;
        this.currency = currency != null ? currency : "USD";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}