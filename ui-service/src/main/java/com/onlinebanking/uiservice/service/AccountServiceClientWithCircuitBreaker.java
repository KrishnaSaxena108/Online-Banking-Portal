package com.onlinebanking.uiservice.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Service
public class AccountServiceClientWithCircuitBreaker {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private CircuitBreaker accountServiceCircuitBreaker;
    
    private static final String ACCOUNT_SERVICE_URL = "http://localhost:8081";
    
    public List<Map<String, Object>> getAccountsByUsername(String username) {
        Supplier<List<Map<String, Object>>> supplier = () -> {
            String url = ACCOUNT_SERVICE_URL + "/accounts/user/" + username;
            return restTemplate.getForObject(url, List.class);
        };
        
        return accountServiceCircuitBreaker.executeSupplier(supplier);
    }
    
    public Map<String, Object> deposit(String accountNumber, Double amount) {
        Supplier<Map<String, Object>> supplier = () -> {
            String url = ACCOUNT_SERVICE_URL + "/accounts/" + accountNumber + "/deposit";
            Map<String, Double> request = new HashMap<>();
            request.put("amount", amount);
            return restTemplate.postForObject(url, request, Map.class);
        };
        
        return accountServiceCircuitBreaker.executeSupplier(supplier);
    }
    
    public Map<String, Object> withdraw(String accountNumber, Double amount) {
        Supplier<Map<String, Object>> supplier = () -> {
            String url = ACCOUNT_SERVICE_URL + "/accounts/" + accountNumber + "/withdraw";
            Map<String, Double> request = new HashMap<>();
            request.put("amount", amount);
            return restTemplate.postForObject(url, request, Map.class);
        };
        
        return accountServiceCircuitBreaker.executeSupplier(supplier);
    }
    
    public List<Map<String, Object>> getAllAccounts() {
        Supplier<List<Map<String, Object>>> supplier = () -> {
            String url = ACCOUNT_SERVICE_URL + "/accounts";
            return restTemplate.getForObject(url, List.class);
        };
        
        return accountServiceCircuitBreaker.executeSupplier(supplier);
    }
    
    // Fallback methods
    public List<Map<String, Object>> getAccountsFallback(String username, Exception ex) {
        System.err.println("Account service circuit breaker activated for user: " + username + ". Error: " + ex.getMessage());
        List<Map<String, Object>> fallbackAccounts = new ArrayList<>();
        Map<String, Object> fallbackAccount = new HashMap<>();
        fallbackAccount.put("accountNumber", "SERVICE_UNAVAILABLE");
        fallbackAccount.put("accountName", "Account Service Currently Unavailable");
        fallbackAccount.put("balance", 0.0);
        fallbackAccount.put("username", username);
        fallbackAccounts.add(fallbackAccount);
        return fallbackAccounts;
    }
    
    public Map<String, Object> depositFallback(String accountNumber, Double amount, Exception ex) {
        System.err.println("Account service circuit breaker activated for deposit. Account: " + accountNumber + ". Error: " + ex.getMessage());
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("error", "Account service is currently unavailable. Please try again later.");
        fallbackResponse.put("accountNumber", accountNumber);
        fallbackResponse.put("amount", amount);
        return fallbackResponse;
    }
    
    public Map<String, Object> withdrawFallback(String accountNumber, Double amount, Exception ex) {
        System.err.println("Account service circuit breaker activated for withdrawal. Account: " + accountNumber + ". Error: " + ex.getMessage());
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("error", "Account service is currently unavailable. Please try again later.");
        fallbackResponse.put("accountNumber", accountNumber);
        fallbackResponse.put("amount", amount);
        return fallbackResponse;
    }
    
    public List<Map<String, Object>> getAllAccountsFallback(Exception ex) {
        System.err.println("Account service circuit breaker activated for getAllAccounts. Error: " + ex.getMessage());
        List<Map<String, Object>> fallbackAccounts = new ArrayList<>();
        Map<String, Object> fallbackAccount = new HashMap<>();
        fallbackAccount.put("accountNumber", "SERVICE_UNAVAILABLE");
        fallbackAccount.put("accountName", "Account Service Currently Unavailable");
        fallbackAccount.put("balance", 0.0);
        fallbackAccounts.add(fallbackAccount);
        return fallbackAccounts;
    }
}