package com.kosi.financetracker.backend.repository;

import com.kosi.financetracker.backend.model.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Basic query methods
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByUserIdAndType(Long userId, String type);
    List<Transaction> findByUserIdAndCategory(Long userId, String category);
    
    // Date range queries
    List<Transaction> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    // For pagination support - needed by DashboardService
    List<Transaction> findByUserIdOrderByDateDesc(Long userId, Pageable pageable);
    
    // Custom query for recent transactions
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId ORDER BY t.date DESC")
    List<Transaction> findTopByUserIdOrderByDateDesc(@Param("userId") Long userId);
    
    // Check if transaction exists for user (for security)
    boolean existsByIdAndUserId(Long id, Long userId);
}