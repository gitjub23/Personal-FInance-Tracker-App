package com.kosi.financetracker.backend.Controller;

import com.kosi.financetracker.backend.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:3000")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getDashboardSummary(@PathVariable Long userId) {
        try {
            Map<String, Object> dashboardData = dashboardService.getDashboardSummary(userId);
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            // Return empty/zero data instead of mock data
            Map<String, Object> emptyData = new HashMap<>();
            emptyData.put("income", 0.0);
            emptyData.put("expenses", 0.0);
            emptyData.put("budget", 0.0);
            emptyData.put("savings", 0.0);
            emptyData.put("transactions", 0);
            emptyData.put("budgetUsed", 0.0);
            return ResponseEntity.ok(emptyData);
        }
    }

    @GetMapping("/user/{userId}/expenses-by-category")
    public ResponseEntity<?> getExpensesByCategory(@PathVariable Long userId) {
        try {
            Map<String, Double> expensesByCategory = dashboardService.getExpensesByCategory(userId);
            return ResponseEntity.ok(expensesByCategory);
        } catch (Exception e) {
            // Return empty map instead of mock data
            return ResponseEntity.ok(new HashMap<String, Double>());
        }
    }

    @GetMapping("/user/{userId}/budget-vs-spending")
    public ResponseEntity<?> getBudgetVsSpending(@PathVariable Long userId) {
        try {
            Map<String, Object> budgetVsSpending = dashboardService.getBudgetVsSpending(userId);
            return ResponseEntity.ok(budgetVsSpending);
        } catch (Exception e) {
            // Return empty data instead of mock data
            Map<String, Object> emptyData = new HashMap<>();
            emptyData.put("labels", new String[]{});
            emptyData.put("budget", new double[]{});
            emptyData.put("spending", new double[]{});
            return ResponseEntity.ok(emptyData);
        }
    }

    @GetMapping("/user/{userId}/recent-transactions")
    public ResponseEntity<?> getRecentTransactions(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(dashboardService.getRecentTransactions(userId, 5));
        } catch (Exception e) {
            return ResponseEntity.ok(new HashMap<>());
        }
    }
}