package com.kosi.financetracker.backend.service;

import com.kosi.financetracker.backend.model.User;
import com.kosi.financetracker.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OAuthService {

    @Autowired
    private UserRepository userRepository;

    public User processOAuthUser(String provider, String oauthId, String email, String name, String profilePicture) {
        Optional<User> existingUser = userRepository.findByOauthProviderAndOauthId(provider, oauthId);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setName(name);
            user.setEmail(email);
            user.setProfilePicture(profilePicture);
            user.setLastLogin(LocalDateTime.now());
            user.setEmailVerified(true);
            return userRepository.save(user);
        }
        
        Optional<User> emailUser = userRepository.findByEmail(email);
        
        if (emailUser.isPresent()) {
            User user = emailUser.get();
            user.setOauthProvider(provider);
            user.setOauthId(oauthId);
            user.setProfilePicture(profilePicture);
            user.setLastLogin(LocalDateTime.now());
            user.setEmailVerified(true);
            return userRepository.save(user);
        }
        
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setOauthProvider(provider);
        newUser.setOauthId(oauthId);
        newUser.setProfilePicture(profilePicture);
        newUser.setEmailVerified(true);
        newUser.setIsActive(true);
        newUser.setLastLogin(LocalDateTime.now());
        
        return userRepository.save(newUser);
    }

    public boolean isEmailRegistered(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public String getOAuthProvider(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.map(User::getOauthProvider).orElse(null);
    }
}