package com.kosi.financetracker.backend.repository;

import com.kosi.financetracker.backend.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    
    // Find all budgets for a user
    List<Budget> findByUserId(Long userId);
    
    // Find budgets by user and month
    List<Budget> findByUserIdAndMonth(Long userId, int month);
    
    // Find budgets by user, month, and year
    @Query("SELECT b FROM Budget b WHERE b.userId = :userId AND b.month = :month AND b.year = :year")
    List<Budget> findByUserIdAndMonthAndYear(@Param("userId") Long userId, 
                                            @Param("month") int month, 
                                            @Param("year") int year);
    
    // Check if budget exists for user and category
    boolean existsByUserIdAndCategory(Long userId, String category);

    // REMOVED: findOverSpentBudgets 
    // (Logic moved to BudgetService because 'spent' is calculated, not stored in DB)

    // REMOVED: findBudgetsCloseToLimit
    // (Logic moved to BudgetService)
}