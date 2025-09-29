package com.onlinebanking.uiservice.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
public class DashboardController {

    @Autowired
    private RestTemplate restTemplate;
    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        // Check if user is authenticated
        String username = (String) session.getAttribute("username");
        
        if (username == null) {
            return "redirect:/login";
        }
        
        try {
            // Fetch account data directly from account service
            String accountServiceUrl = "http://localhost:8081/accounts/user/" + username;
            ResponseEntity<List> accountResponse = restTemplate.getForEntity(accountServiceUrl, List.class);
            
            if (accountResponse.getStatusCode() == HttpStatus.OK && accountResponse.getBody() != null) {
                List<Map<String, Object>> accounts = (List<Map<String, Object>>) accountResponse.getBody();
                if (!accounts.isEmpty()) {
                    // Get first account data
                    Map<String, Object> accountData = accounts.get(0);
                    model.addAttribute("currentUser", username);
                    model.addAttribute("accountNumber", accountData.get("accountNumber"));
                    model.addAttribute("balance", accountData.get("balance"));
                    model.addAttribute("accountType", accountData.get("accountType"));
                } else {
                    // User has no accounts - create a default one
                    model.addAttribute("currentUser", username);
                    model.addAttribute("accountNumber", "No Account");
                    model.addAttribute("balance", "0.00");
                    model.addAttribute("accountType", "Student Savings");
                }
            } else {
                // Service unavailable fallback
                model.addAttribute("currentUser", username);
                model.addAttribute("accountNumber", "Service Unavailable");
                model.addAttribute("balance", "0.00");
                model.addAttribute("accountType", "Student Savings");
            }
            
            // Try to fetch transactions from transaction service
            try {
                String transactionServiceUrl = "http://localhost:8082/transactions/user/" + username;
                ResponseEntity<List> transactionResponse = restTemplate.getForEntity(transactionServiceUrl, List.class);
                
                if (transactionResponse.getStatusCode() == HttpStatus.OK && transactionResponse.getBody() != null) {
                    model.addAttribute("transactions", transactionResponse.getBody());
                } else {
                    model.addAttribute("transactions", List.of());
                }
            } catch (Exception e) {
                // Transactions service might not be available
                model.addAttribute("transactions", List.of());
            }
            
        } catch (Exception e) {
            // Service call failed - show user but with error info
            model.addAttribute("currentUser", username);
            model.addAttribute("accountNumber", "Error: " + e.getMessage());
            model.addAttribute("balance", "0.00");
            model.addAttribute("accountType", "Error");
            model.addAttribute("transactions", List.of());
            model.addAttribute("error", "Unable to fetch account data: " + e.getMessage());
        }
        
        return "dashboard";
    }
    
    @PostMapping("/dashboard/transfer")
    public String transfer(@RequestParam String toAccountNumber, 
                          @RequestParam double amount,
                          @RequestParam(required = false) String description,
                          HttpSession session, Model model) {
        
        String username = (String) session.getAttribute("username");
        
        if (username == null) {
            return "redirect:/login";
        }
        
        try {
            // First get user's account number
            String accountServiceUrl = "http://localhost:8081/accounts/user/" + username;
            ResponseEntity<List> accountResponse = restTemplate.getForEntity(accountServiceUrl, List.class);
            
            if (accountResponse.getStatusCode() == HttpStatus.OK && accountResponse.getBody() != null) {
                List<Map<String, Object>> accounts = (List<Map<String, Object>>) accountResponse.getBody();
                if (!accounts.isEmpty()) {
                    String fromAccount = (String) accounts.get(0).get("accountNumber");
                    
                    // Create transfer request
                    Map<String, Object> transferRequest = new HashMap<>();
                    transferRequest.put("fromAccountNumber", fromAccount);
                    transferRequest.put("toAccountNumber", toAccountNumber);
                    transferRequest.put("amount", amount);
                    transferRequest.put("description", description != null ? description : "Transfer");
                    
                    // Call account service transfer endpoint
                    String transferUrl = "http://localhost:8081/accounts/transfer";
                    ResponseEntity<Map> response = restTemplate.postForEntity(transferUrl, transferRequest, Map.class);
                    
                    if (response.getStatusCode() == HttpStatus.OK) {
                        model.addAttribute("success", "Transfer successful!");
                    } else {
                        Map<String, Object> errorResponse = response.getBody();
                        String errorMessage = errorResponse != null ? (String) errorResponse.get("error") : "Transfer failed";
                        model.addAttribute("error", errorMessage);
                    }
                } else {
                    model.addAttribute("error", "No account found for user.");
                }
            } else {
                model.addAttribute("error", "Unable to fetch account information.");
            }
            
        } catch (Exception e) {
            model.addAttribute("error", "Transfer failed: " + e.getMessage());
        }
        
        return "redirect:/dashboard";
    }
    
    @PostMapping("/dashboard/deposit")
    public String deposit(@RequestParam double amount,
                         @RequestParam(required = false) String description,
                         HttpSession session, Model model) {
        
        String username = (String) session.getAttribute("username");
        
        if (username == null) {
            return "redirect:/login";
        }
        
        try {
            // First get user's account number
            String accountServiceUrl = "http://localhost:8081/accounts/user/" + username;
            ResponseEntity<List> accountResponse = restTemplate.getForEntity(accountServiceUrl, List.class);
            
            if (accountResponse.getStatusCode() == HttpStatus.OK && accountResponse.getBody() != null) {
                List<Map<String, Object>> accounts = (List<Map<String, Object>>) accountResponse.getBody();
                if (!accounts.isEmpty()) {
                    String accountNumber = (String) accounts.get(0).get("accountNumber");
                    
                    // Create deposit request
                    Map<String, Object> depositRequest = new HashMap<>();
                    depositRequest.put("amount", amount);
                    
                    // Call account service deposit endpoint
                    String depositUrl = "http://localhost:8081/accounts/" + accountNumber + "/deposit";
                    ResponseEntity<String> response = restTemplate.postForEntity(depositUrl, depositRequest, String.class);
                    
                    if (response.getStatusCode() == HttpStatus.OK) {
                        model.addAttribute("success", "Deposit successful!");
                    } else {
                        model.addAttribute("error", "Deposit failed. Please try again.");
                    }
                } else {
                    model.addAttribute("error", "No account found for user.");
                }
            } else {
                model.addAttribute("error", "Unable to fetch account information.");
            }
            
        } catch (Exception e) {
            model.addAttribute("error", "Deposit failed: " + e.getMessage());
        }
        
        return "redirect:/dashboard";
    }
    
    @PostMapping("/dashboard/withdraw")
    public String withdraw(@RequestParam double amount,
                          @RequestParam(required = false) String description,
                          HttpSession session, Model model) {
        
        String username = (String) session.getAttribute("username");
        
        if (username == null) {
            return "redirect:/login";
        }
        
        try {
            // First get user's account number
            String accountServiceUrl = "http://localhost:8081/accounts/user/" + username;
            ResponseEntity<List> accountResponse = restTemplate.getForEntity(accountServiceUrl, List.class);
            
            if (accountResponse.getStatusCode() == HttpStatus.OK && accountResponse.getBody() != null) {
                List<Map<String, Object>> accounts = (List<Map<String, Object>>) accountResponse.getBody();
                if (!accounts.isEmpty()) {
                    String accountNumber = (String) accounts.get(0).get("accountNumber");
                    
                    // Create withdrawal request
                    Map<String, Object> withdrawRequest = new HashMap<>();
                    withdrawRequest.put("amount", amount);
                    
                    // Call account service withdrawal endpoint
                    String withdrawUrl = "http://localhost:8081/accounts/" + accountNumber + "/withdraw";
                    ResponseEntity<String> response = restTemplate.postForEntity(withdrawUrl, withdrawRequest, String.class);
                    
                    if (response.getStatusCode() == HttpStatus.OK) {
                        model.addAttribute("success", "Withdrawal successful!");
                    } else {
                        model.addAttribute("error", "Withdrawal failed. Please try again.");
                    }
                } else {
                    model.addAttribute("error", "No account found for user.");
                }
            } else {
                model.addAttribute("error", "Unable to fetch account information.");
            }
            
        } catch (Exception e) {
            model.addAttribute("error", "Insufficient funds or withdrawal failed: " + e.getMessage());
        }
        
        return "redirect:/dashboard";
    }
}
