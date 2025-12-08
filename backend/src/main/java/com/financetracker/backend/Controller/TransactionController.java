package com.kosi.financetracker.backend.Controller;

import com.kosi.financetracker.backend.model.Transaction;
import com.kosi.financetracker.backend.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:3000")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // Create a new transaction
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {
        try {
            Transaction createdTransaction = transactionService.createTransaction(transaction);
            return ResponseEntity.ok(createdTransaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get all transactions for a user - FIXED method name
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getTransactionsByUser(@PathVariable Long userId) {
        List<Transaction> transactions = transactionService.getTransactionsByUserId(userId);
        return ResponseEntity.ok(transactions);
    }

    // Get a single transaction by ID
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        Optional<Transaction> transaction = transactionService.getTransactionById(id);
        return transaction.map(ResponseEntity::ok)
                         .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update a transaction
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(
            @PathVariable Long id,
            @RequestBody Transaction transaction) {
        try {
            Transaction updatedTransaction = transactionService.updateTransaction(id, transaction);
            return ResponseEntity.ok(updatedTransaction);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a transaction - FIXED: properly handle void method
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        try {
            // Check if transaction exists before deleting
            if (transactionService.transactionExists(id)) {
                transactionService.deleteTransaction(id);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get transactions by type (income/expense)
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<Transaction>> getTransactionsByType(
            @PathVariable Long userId,
            @PathVariable String type) {
        List<Transaction> transactions = transactionService.getTransactionsByUserId(userId);
        List<Transaction> filteredTransactions = transactions.stream()
                .filter(t -> type.equalsIgnoreCase(t.getType()))
                .toList();
        return ResponseEntity.ok(filteredTransactions);
    }
}