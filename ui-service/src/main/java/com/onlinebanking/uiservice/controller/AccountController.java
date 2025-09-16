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
import java.time.LocalDateTime;

@Controller
@SessionAttributes("username")
public class AccountController {
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/accounts")
    public String accounts(Model model, @SessionAttribute("username") String username) {
        // Fetch accounts for the logged-in user
        List<Map<String, Object>> accounts = restTemplate.getForObject(
            "http://localhost:8081/accounts/user/" + username, List.class);
        model.addAttribute("accounts", accounts);
        return "accounts";
    }

    @GetMapping("/transactions")
    public String transactions(Model model, @SessionAttribute("accountNumber") String accountNumber) {
        // Fetch only transactions for the user's account number
        String url = "http://localhost:8082/transactions/account/" + accountNumber;
        List<Map<String, Object>> transactions = restTemplate.getForObject(url, List.class);
        model.addAttribute("transactions", transactions);
        return "transactions";
    }

    @PostMapping("/deposit")
    @ResponseBody
    public ResponseEntity<String> deposit(@RequestParam String accountNumber, 
                                        @RequestParam Double amount,
                                        @SessionAttribute("username") String username) {
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
                                         @SessionAttribute("username") String username) {
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
