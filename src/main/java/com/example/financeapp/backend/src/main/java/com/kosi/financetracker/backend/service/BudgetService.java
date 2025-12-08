package com.kosi.financetracker.backend.service;

import com.kosi.financetracker.backend.model.Budget;
import com.kosi.financetracker.backend.repository.BudgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    // Use @Lazy to prevent circular dependency if DashboardService also uses BudgetService
    @Autowired 
    @Lazy
    private DashboardService dashboardService;

    public List<Budget> getBudgetsByUserId(Long userId) {
        return budgetRepository.findByUserId(userId);
    }
    
    public List<Budget> getBudgetsByUserIdAndMonth(Long userId, int month, int year) {
        return budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);
    }

    /**
     * New method to find overspent budgets in Java
     */
    public List<Budget> getOverSpentBudgets(Long userId) {
        LocalDate now = LocalDate.now();
        // Get this month's budgets
        List<Budget> budgets = getBudgetsByUserIdAndMonth(userId, now.getMonthValue(), now.getYear());
        
        // Calculate spending and filter
        return budgets.stream()
            .peek(budget -> {
                double spent = dashboardService.calculateSpendingForBudget(
                    userId, budget.getCategory(), budget.getMonth(), budget.getYear()
                );
                budget.setSpent(spent);
            })
            .filter(Budget::isOverBudget) // Uses the logic inside Budget.java
            .collect(Collectors.toList());
    }

    public Budget createBudget(Budget budget) {
        validateBudget(budget);
        
        List<Budget> existing = budgetRepository.findByUserIdAndMonthAndYear(
            budget.getUserId(), 
            budget.getMonth(), 
            budget.getYear()
        );
        
        for (Budget b : existing) {
            if (b.getCategory().equalsIgnoreCase(budget.getCategory())) {
                throw new RuntimeException("Budget already exists for " + budget.getCategory() + 
                    " in " + budget.getMonth() + "/" + budget.getYear());
            }
        }
        
        return budgetRepository.save(budget);
    }

    public Budget updateBudget(Long id, Budget budgetDetails) {
        Optional<Budget> budgetOptional = budgetRepository.findById(id);
        
        if (budgetOptional.isPresent()) {
            Budget existingBudget = budgetOptional.get();
            validateBudget(budgetDetails);
            
            existingBudget.setCategory(budgetDetails.getCategory());
            existingBudget.setLimitAmount(budgetDetails.getLimitAmount());
            existingBudget.setColor(budgetDetails.getColor());
            existingBudget.setCurrency(budgetDetails.getCurrency());
            existingBudget.setMonth(budgetDetails.getMonth());
            existingBudget.setYear(budgetDetails.getYear());
            
            return budgetRepository.save(existingBudget);
        } else {
            throw new RuntimeException("Budget not found with id: " + id);
        }
    }

    public void deleteBudget(Long id) {
        if (!budgetRepository.existsById(id)) {
            throw new RuntimeException("Budget not found with id: " + id);
        }
        budgetRepository.deleteById(id);
    }

    public Map<String, Object> getBudgetStatus(Long userId) {
        Map<String, Object> status = new HashMap<>();
        
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();
        
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, currentMonth, currentYear);
        
        status.put("totalBudgets", budgets.size());
        status.put("currentMonth", currentMonth);
        status.put("currentYear", currentYear);
        
        // Add Overspent count here using our new Java logic
        long overspentCount = budgets.stream()
            .map(b -> {
                double spent = dashboardService.calculateSpendingForBudget(userId, b.getCategory(), currentMonth, currentYear);
                b.setSpent(spent);
                return b;
            })
            .filter(Budget::isOverBudget)
            .count();

        status.put("overBudgetCount", overspentCount);
        
        return status;
    }

    private void validateBudget(Budget budget) {
        if (budget.getCategory() == null || budget.getCategory().trim().isEmpty()) {
            throw new RuntimeException("Budget category is required");
        }
        if (budget.getLimitAmount() == null || budget.getLimitAmount() <= 0) {
            throw new RuntimeException("Budget limit must be positive");
        }
        if (budget.getUserId() == null) {
            throw new RuntimeException("User ID is required");
        }
        if (budget.getMonth() == null || budget.getMonth() < 1 || budget.getMonth() > 12) {
            throw new RuntimeException("Valid month (1-12) is required");
        }
        if (budget.getYear() == null || budget.getYear() < 2000) {
            throw new RuntimeException("Valid year is required");
        }
    }
}