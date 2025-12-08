package com.kosi.financetracker.backend.Controller;

import com.kosi.financetracker.backend.model.Budget;
import com.kosi.financetracker.backend.service.BudgetService;
import com.kosi.financetracker.backend.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
@CrossOrigin(origins = "http://localhost:3000")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;
    
    @Autowired
    private DashboardService dashboardService;

    // GET all budgets for current month
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserBudgets(@PathVariable Long userId) {
        try {
            LocalDate now = LocalDate.now();
            int currentMonth = now.getMonthValue();
            int currentYear = now.getYear();
            
            List<Budget> budgets = budgetService.getBudgetsByUserIdAndMonth(userId, currentMonth, currentYear);
            
            // Calculate spending for each budget
            for (Budget budget : budgets) {
                double spending = dashboardService.calculateSpendingForBudget(
                    userId, 
                    budget.getCategory(), 
                    budget.getMonth(), 
                    budget.getYear()
                );
                budget.setSpent(spending);
            }
            
            return ResponseEntity.ok(budgets);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch budgets: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // GET budgets for specific month
    @GetMapping("/user/{userId}/month/{month}/year/{year}")
    public ResponseEntity<?> getUserBudgetsByMonth(
            @PathVariable Long userId,
            @PathVariable int month,
            @PathVariable int year) {
        try {
            List<Budget> budgets = budgetService.getBudgetsByUserIdAndMonth(userId, month, year);
            
            // Calculate spending for each budget
            for (Budget budget : budgets) {
                double spending = dashboardService.calculateSpendingForBudget(
                    userId, 
                    budget.getCategory(), 
                    month, 
                    year
                );
                budget.setSpent(spending);
            }
            
            return ResponseEntity.ok(budgets);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch budgets: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // CREATE new budget
    @PostMapping
    public ResponseEntity<?> createBudget(@RequestBody Budget budget) {
        try {
            // Set current month/year if not provided
            if (budget.getMonth() == null) {
                budget.setMonth(LocalDate.now().getMonthValue());
            }
            if (budget.getYear() == null) {
                budget.setYear(LocalDate.now().getYear());
            }
            
            Budget createdBudget = budgetService.createBudget(budget);
            
            // Calculate initial spending
            double spending = dashboardService.calculateSpendingForBudget(
                budget.getUserId(), 
                budget.getCategory(), 
                budget.getMonth(), 
                budget.getYear()
            );
            createdBudget.setSpent(spending);
            
            return ResponseEntity.ok(createdBudget);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create budget: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // UPDATE budget
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBudget(@PathVariable Long id, @RequestBody Budget budget) {
        try {
            Budget updatedBudget = budgetService.updateBudget(id, budget);
            
            // Calculate spending
            double spending = dashboardService.calculateSpendingForBudget(
                updatedBudget.getUserId(), 
                updatedBudget.getCategory(), 
                updatedBudget.getMonth(), 
                updatedBudget.getYear()
            );
            updatedBudget.setSpent(spending);
            
            return ResponseEntity.ok(updatedBudget);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update budget: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // DELETE budget
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBudget(@PathVariable Long id) {
        try {
            budgetService.deleteBudget(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Budget deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete budget: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // GET budget status (for warnings/alerts)
    @GetMapping("/user/{userId}/status")
    public ResponseEntity<?> getBudgetStatus(@PathVariable Long userId) {
        try {
            Map<String, Object> budgetStatus = budgetService.getBudgetStatus(userId);
            return ResponseEntity.ok(budgetStatus);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch budget status: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}