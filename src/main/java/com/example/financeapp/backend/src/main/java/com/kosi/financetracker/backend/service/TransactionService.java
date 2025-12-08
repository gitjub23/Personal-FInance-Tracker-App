package com.kosi.financetracker.backend.service;

import com.kosi.financetracker.backend.model.Transaction;
import com.kosi.financetracker.backend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    // Create a new transaction
    public Transaction createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    // Get all transactions for a user
    public List<Transaction> getTransactionsByUserId(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    // Get a single transaction by ID
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    // Update a transaction - CORRECTED: Transaction has 'name' not 'description'
    public Transaction updateTransaction(Long id, Transaction updatedTransaction) {
        Optional<Transaction> existingTransaction = transactionRepository.findById(id);
        
        if (existingTransaction.isPresent()) {
            Transaction transaction = existingTransaction.get();
            transaction.setName(updatedTransaction.getName());
            transaction.setAmount(updatedTransaction.getAmount());
            transaction.setCategory(updatedTransaction.getCategory());
            transaction.setDate(updatedTransaction.getDate());
            transaction.setType(updatedTransaction.getType());
            
            return transactionRepository.save(transaction);
        }
        
        throw new RuntimeException("Transaction not found with id: " + id);
    }

    // Delete a transaction
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }

    // Check if transaction exists
    public boolean transactionExists(Long id) {
        return transactionRepository.existsById(id);
    }
}