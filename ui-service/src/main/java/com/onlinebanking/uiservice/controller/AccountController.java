package com.onlinebanking.uiservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.LocalDateTime;

@Controller
@SessionAttributes("username")
public class AccountController {
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/accounts")
    public String accounts(Model model, @SessionAttribute(value = "username", required = false) String username) {
        // Check if user is logged in
        if (username == null || username.isEmpty()) {
            return "redirect:/login";
        }
        
        try {
            // Fetch accounts for the logged-in user
            List<Map<String, Object>> accounts = restTemplate.getForObject(
                "http://localhost:8081/accounts/user/" + username, List.class);
            
            // Handle null response
            if (accounts == null) {
                accounts = new ArrayList<>();
            }
            
            model.addAttribute("accounts", accounts);
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // Service unavailable
            System.err.println("Account service unavailable: " + e.getMessage());
            model.addAttribute("accounts", new ArrayList<>());
            model.addAttribute("error", "Account service is currently unavailable. Please ensure the account service is running on port 8081.");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // HTTP error (4xx, 5xx)
            System.err.println("HTTP error fetching accounts: " + e.getMessage());
            model.addAttribute("accounts", new ArrayList<>());
            model.addAttribute("error", "Error fetching accounts: " + e.getStatusCode() + " - " + e.getStatusText());
        } catch (Exception e) {
            System.err.println("Error fetching accounts: " + e.getMessage());
            model.addAttribute("accounts", new ArrayList<>());
            model.addAttribute("error", "Unexpected error occurred: " + e.getMessage());
        }
        
        return "accounts";
    }

    // Health check endpoint for testing services
    @GetMapping("/test-services")
    @ResponseBody
    public ResponseEntity<String> testServices() {
        StringBuilder result = new StringBuilder();
        
        try {
            // Test Account Service
            String accountUrl = "http://localhost:8081/accounts";
            Object accountResponse = restTemplate.getForObject(accountUrl, Object.class);
            result.append("✅ Account Service (8081): OK\n");
        } catch (Exception e) {
            result.append("❌ Account Service (8081): " + e.getMessage() + "\n");
        }
        
        try {
            // Test Transaction Service
            String transactionUrl = "http://localhost:8082/transactions";
            Object transactionResponse = restTemplate.getForObject(transactionUrl, Object.class);
            result.append("✅ Transaction Service (8082): OK\n");
        } catch (Exception e) {
            result.append("❌ Transaction Service (8082): " + e.getMessage() + "\n");
        }
        
        return ResponseEntity.ok(result.toString());
    }

    // Debug endpoint for testing transaction filtering
    @GetMapping("/debug-transactions")
    @ResponseBody
    public ResponseEntity<String> debugTransactions(@RequestParam(required = false) String username,
                                                   @RequestParam(required = false) String accountNumber) {
        StringBuilder result = new StringBuilder();
        result.append("=== TRANSACTION DEBUG ENDPOINT ===\n\n");
        
        if (username == null) username = "testuser"; // default for testing
        
        result.append("Testing with username: ").append(username).append("\n");
        result.append("Testing with accountNumber: ").append(accountNumber != null ? accountNumber : "ALL").append("\n\n");
        
        try {
            if (accountNumber != null && !accountNumber.isEmpty()) {
                String url = "http://localhost:8082/transactions/account/" + accountNumber;
                result.append("Testing URL: ").append(url).append("\n");
                List<Map<String, Object>> transactions = restTemplate.getForObject(url, List.class);
                result.append("Result: ").append(transactions != null ? transactions.size() + " transactions found" : "null response").append("\n");
                if (transactions != null && !transactions.isEmpty()) {
                    result.append("Sample transaction: ").append(transactions.get(0).toString()).append("\n");
                }
            } else {
                String url = "http://localhost:8082/transactions/user/" + username;
                result.append("Testing URL: ").append(url).append("\n");
                List<Map<String, Object>> transactions = restTemplate.getForObject(url, List.class);
                result.append("Result: ").append(transactions != null ? transactions.size() + " transactions found" : "null response").append("\n");
                if (transactions != null && !transactions.isEmpty()) {
                    result.append("Sample transaction: ").append(transactions.get(0).toString()).append("\n");
                }
            }
        } catch (Exception e) {
            result.append("ERROR: ").append(e.getMessage()).append("\n");
            result.append("Exception type: ").append(e.getClass().getSimpleName()).append("\n");
        }
        
        return ResponseEntity.ok(result.toString());
    }

    @GetMapping("/transactions")
    public String transactions(Model model, 
                             @SessionAttribute(value = "username", required = false) String username,
                             @RequestParam(required = false) String accountNumber,
                             HttpSession session) {
        
        // Debug logging
        System.out.println("=== TRANSACTIONS DEBUG ===");
        System.out.println("Username from session: " + username);
        System.out.println("Account number parameter: " + accountNumber);
        
        // Check if user is logged in
        if (username == null || username.isEmpty()) {
            System.out.println("No username in session, redirecting to login");
            return "redirect:/login";
        }
        
        List<Map<String, Object>> transactions = null;
        String serviceUrl = "";
        
        try {
            if (accountNumber != null && !accountNumber.isEmpty()) {
                // Fetch transactions for a specific account
                serviceUrl = "http://localhost:8082/transactions/account/" + accountNumber;
                System.out.println("Fetching transactions for account: " + accountNumber);
                System.out.println("Service URL: " + serviceUrl);
                transactions = restTemplate.getForObject(serviceUrl, List.class);
            } else {
                // Fetch all transactions for the logged-in user
                serviceUrl = "http://localhost:8082/transactions/user/" + username;
                System.out.println("Fetching all transactions for user: " + username);
                System.out.println("Service URL: " + serviceUrl);
                transactions = restTemplate.getForObject(serviceUrl, List.class);
            }
            
            System.out.println("Transactions received: " + (transactions != null ? transactions.size() : "null"));
            
            // Handle null response
            if (transactions == null) {
                transactions = new ArrayList<>();
                System.out.println("Null response from service, using empty list");
            }
            
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // Service unavailable
            System.err.println("Transaction service unavailable: " + e.getMessage());
            transactions = new ArrayList<>();
            model.addAttribute("error", "Transaction service is currently unavailable. Please ensure the transaction service is running on port 8082. Service URL: " + serviceUrl);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // HTTP error (4xx, 5xx)
            System.err.println("HTTP error fetching transactions: " + e.getMessage());
            transactions = new ArrayList<>();
            if (e.getStatusCode().value() == 404) {
                if (accountNumber != null) {
                    model.addAttribute("error", "No transactions found for account: " + accountNumber);
                } else {
                    model.addAttribute("error", "No transactions found for user: " + username);
                }
            } else {
                model.addAttribute("error", "Error fetching transactions: " + e.getStatusCode() + " - " + e.getStatusText() + ". Service URL: " + serviceUrl);
            }
        } catch (Exception e) {
            // Other errors
            System.err.println("Error fetching transactions: " + e.getMessage());
            e.printStackTrace();
            transactions = new ArrayList<>();
            model.addAttribute("error", "Unexpected error occurred: " + e.getMessage() + ". Service URL: " + serviceUrl);
        }
        
        System.out.println("Final transactions count: " + transactions.size());
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("selectedAccountNumber", accountNumber);
        return "transactions";
    }

    @PostMapping("/deposit")
    @ResponseBody
    public ResponseEntity<String> deposit(@RequestParam String accountNumber, 
                                        @RequestParam Double amount,
                                        @SessionAttribute(value = "username", required = false) String username) {
        // Check if user is logged in
        if (username == null || username.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in first");
        }
        
        try {
            // Call account service to deposit money
            Map<String, Double> request = new HashMap<>();
            request.put("amount", amount);
            
            ResponseEntity<Map> accountResponse = restTemplate.postForEntity(
                "http://localhost:8081/accounts/" + accountNumber + "/deposit", 
                request, Map.class);
            
            if (accountResponse.getStatusCode() == HttpStatus.OK) {
                // Create transaction record
                Map<String, Object> transaction = new HashMap<>();
                transaction.put("accountNumber", accountNumber);
                transaction.put("amount", amount);
                transaction.put("type", "DEPOSIT");
                transaction.put("timestamp", LocalDateTime.now());
                transaction.put("username", username);
                
                restTemplate.postForEntity("http://localhost:8082/transactions", transaction, Map.class);
                
                return ResponseEntity.ok("Deposit successful");
            } else {
                return ResponseEntity.badRequest().body("Deposit failed");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/withdraw")
    @ResponseBody
    public ResponseEntity<String> withdraw(@RequestParam String accountNumber, 
                                         @RequestParam Double amount,
                                         @SessionAttribute(value = "username", required = false) String username) {
        // Check if user is logged in
        if (username == null || username.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in first");
        }
        
        try {
            // Call account service to withdraw money
            Map<String, Double> request = new HashMap<>();
            request.put("amount", amount);
            
            ResponseEntity<Map> accountResponse = restTemplate.postForEntity(
                "http://localhost:8081/accounts/" + accountNumber + "/withdraw", 
                request, Map.class);
            
            if (accountResponse.getStatusCode() == HttpStatus.OK) {
                // Create transaction record
                Map<String, Object> transaction = new HashMap<>();
                transaction.put("accountNumber", accountNumber);
                transaction.put("amount", amount);
                transaction.put("type", "WITHDRAWAL");
                transaction.put("timestamp", LocalDateTime.now());
                transaction.put("username", username);
                
                restTemplate.postForEntity("http://localhost:8082/transactions", transaction, Map.class);
                
                return ResponseEntity.ok("Withdrawal successful");
            } else {
                return ResponseEntity.badRequest().body("Insufficient funds or invalid account");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
