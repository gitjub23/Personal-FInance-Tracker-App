package com.kosi.financetracker.backend.service;

import com.kosi.financetracker.backend.model.Transaction;
import com.kosi.financetracker.backend.model.Budget;
import com.kosi.financetracker.backend.repository.TransactionRepository;
import com.kosi.financetracker.backend.repository.BudgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BudgetRepository budgetRepository;
    
    @Autowired
    private CurrencyConversionService currencyService;

    public Map<String, Object> getDashboardSummary(Long userId) {
        Map<String, Object> summary = new HashMap<>();
        
        // Get all transactions for the user
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        
        // Calculate totals (all in USD for consistency)
        double totalIncome = transactions.stream()
                .filter(t -> "income".equalsIgnoreCase(t.getType()))
                .mapToDouble(t -> currencyService.convert(t.getAmount(), t.getCurrency(), "USD"))
                .sum();
                
        double totalExpenses = transactions.stream()
                .filter(t -> "expense".equalsIgnoreCase(t.getType()))
                .mapToDouble(t -> currencyService.convert(t.getAmount(), t.getCurrency(), "USD"))
                .sum();
        
        // Get current month's budget and spending
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();
        
        List<Budget> monthlyBudgets = budgetRepository.findByUserIdAndMonthAndYear(userId, currentMonth, currentYear);
        
        double monthlyBudget = monthlyBudgets.stream()
                .mapToDouble(b -> currencyService.convert(b.getLimitAmount(), b.getCurrency(), "USD"))
                .sum();
        
        // Calculate current month's spending
        LocalDate startOfMonth = LocalDate.of(currentYear, currentMonth, 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);
        
        double monthlySpending = transactionRepository.findByUserIdAndDateBetween(userId, startOfMonth, endOfMonth)
                .stream()
                .filter(t -> "expense".equalsIgnoreCase(t.getType()))
                .mapToDouble(t -> currencyService.convert(t.getAmount(), t.getCurrency(), "USD"))
                .sum();
        
        double savings = totalIncome - totalExpenses;
        double budgetUsed = monthlyBudget > 0 ? (monthlySpending / monthlyBudget) * 100 : 0;
        
        summary.put("income", Math.round(totalIncome * 100.0) / 100.0);
        summary.put("expenses", Math.round(totalExpenses * 100.0) / 100.0);
        summary.put("budget", Math.round(monthlyBudget * 100.0) / 100.0);
        summary.put("savings", Math.round(savings * 100.0) / 100.0);
        summary.put("transactions", transactions.size());
        summary.put("budgetUsed", Math.round(budgetUsed * 100.0) / 100.0);
        summary.put("monthlySpending", Math.round(monthlySpending * 100.0) / 100.0);
        summary.put("currency", "USD"); // Always return in USD, frontend will convert
        
        return summary;
    }

    public Map<String, Double> getExpensesByCategory(Long userId) {
        List<Transaction> expenses = transactionRepository.findByUserIdAndType(userId, "expense");
        
        // Group by category and convert all to USD
        return expenses.stream()
                .collect(Collectors.groupingBy(
                    Transaction::getCategory,
                    Collectors.summingDouble(t -> currencyService.convert(t.getAmount(), t.getCurrency(), "USD"))
                ));
    }

    public Map<String, Object> getBudgetVsSpending(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        LocalDate now = LocalDate.now();
        String[] labels = new String[6];
        double[] budgetData = new double[6];
        double[] spendingData = new double[6];
        
        for (int i = 0; i < 6; i++) {
            LocalDate month = now.minusMonths(5 - i);
            int monthValue = month.getMonthValue();
            int year = month.getYear();
            
            labels[i] = month.getMonth().toString().substring(0, 3);
            
            // Get budget for this month (convert to USD)
            double budget = budgetRepository.findByUserIdAndMonthAndYear(userId, monthValue, year)
                    .stream()
                    .mapToDouble(b -> currencyService.convert(b.getLimitAmount(), b.getCurrency(), "USD"))
                    .sum();
            budgetData[i] = Math.round(budget * 100.0) / 100.0;
            
            // Get spending for this month (convert to USD)
            LocalDate startDate = LocalDate.of(year, monthValue, 1);
            LocalDate endDate = startDate.plusMonths(1).minusDays(1);
            
            double spending = transactionRepository.findByUserIdAndDateBetween(userId, startDate, endDate)
                    .stream()
                    .filter(t -> "expense".equalsIgnoreCase(t.getType()))
                    .mapToDouble(t -> currencyService.convert(t.getAmount(), t.getCurrency(), "USD"))
                    .sum();
            spendingData[i] = Math.round(spending * 100.0) / 100.0;
        }
        
        result.put("labels", labels);
        result.put("budget", budgetData);
        result.put("spending", spendingData);
        
        return result;
    }

    public List<Transaction> getRecentTransactions(Long userId, int limit) {
        return transactionRepository.findByUserIdOrderByDateDesc(userId, PageRequest.of(0, limit));
    }
    
    /**
     * Calculate spending for a specific budget category and month
     */
    public double calculateSpendingForBudget(Long userId, String category, int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        return transactionRepository.findByUserIdAndDateBetween(userId, startDate, endDate)
                .stream()
                .filter(t -> "expense".equalsIgnoreCase(t.getType()))
                .filter(t -> category.equalsIgnoreCase(t.getCategory()))
                .mapToDouble(t -> currencyService.convert(t.getAmount(), t.getCurrency(), "USD"))
                .sum();
    }
}