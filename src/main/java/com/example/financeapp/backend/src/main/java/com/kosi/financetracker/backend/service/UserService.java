package com.kosi.financetracker.backend.service;

import com.kosi.financetracker.backend.model.User;
import com.kosi.financetracker.backend.Controller.AuthController.UserRegistrationRequest;
import com.kosi.financetracker.backend.repository.UserRepository;
import com.kosi.financetracker.backend.repository.TransactionRepository;
import com.kosi.financetracker.backend.repository.BudgetRepository;
import com.kosi.financetracker.backend.repository.UserPreferencesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserPreferencesRepository preferencesRepository;

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public User createUser(UserRegistrationRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        
        return createUser(user);
    }

    public User createUser(User user) {
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new RuntimeException("Name is required");
        }

        if (existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        return userRepository.save(user);
    }

    public User authenticateUser(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Verify current password
            if (!user.getPassword().equals(currentPassword)) {
                return false;
            }
            
            // Validate new password
            if (newPassword == null || newPassword.trim().length() < 6) {
                throw new RuntimeException("New password must be at least 6 characters");
            }
            
            // Update password
            user.setPassword(newPassword);
            userRepository.save(user);
            return true;
        }
        
        throw new RuntimeException("User not found");
    }

    @Transactional
    public boolean deleteAccount(Long userId, String password) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Verify password
            if (!user.getPassword().equals(password)) {
                return false;
            }
            
            // Delete all related data
            transactionRepository.deleteAll(transactionRepository.findByUserId(userId));
            budgetRepository.deleteAll(budgetRepository.findByUserId(userId));
            preferencesRepository.findByUserId(userId).ifPresent(prefs -> 
                preferencesRepository.delete(prefs)
            );
            
            // Delete user
            userRepository.delete(user);
            return true;
        }
        
        throw new RuntimeException("User not found");
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}