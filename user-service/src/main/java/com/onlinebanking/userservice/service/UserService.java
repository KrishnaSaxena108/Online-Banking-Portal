package com.onlinebanking.userservice.service;

import com.onlinebanking.userservice.model.User;
import com.onlinebanking.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public boolean register(String username, String password, String accountNumber) {
        if (userRepository.findByUsername(username).isPresent()) {
            return false;
        }
        if (userRepository.findAll().stream().anyMatch(u -> u.getAccountNumber().equals(accountNumber))) {
            return false;
        }
        
        // Create user
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setAccountNumber(accountNumber);
        userRepository.save(user);
        
        // Create corresponding account in Account Service
        try {
            Map<String, Object> accountData = new HashMap<>();
            accountData.put("accountNumber", accountNumber);
            accountData.put("accountHolderName", username);
            accountData.put("balance", 0.0); // Initial balance
            accountData.put("username", username);
            
            restTemplate.postForObject("http://localhost:8081/accounts", accountData, Object.class);
        } catch (Exception e) {
            System.err.println("Failed to create account in Account Service: " + e.getMessage());
            // Continue - user creation successful, account creation can be retried later
        }
        
        return true;
    }

    public Optional<User> authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            return userOpt;
        }
        return Optional.empty();
    }
}
