package com.onlinebanking.uiservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.onlinebanking.uiservice.service.UserServiceClient;
import com.onlinebanking.uiservice.service.AccountServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private AccountServiceClient accountServiceClient;

    @GetMapping({"/", "/login"})
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model, HttpSession session) {
        Map<String, String> req = new HashMap<>();
        req.put("username", username);
        req.put("password", password);
        try {
            Map<String, Object> response = userServiceClient.login(req);
            if (response.containsKey("token")) {
                // Store username in session
                session.setAttribute("username", username);
                return "redirect:/dashboard";
            } else {
                model.addAttribute("error", response.getOrDefault("error", "Login failed"));
                return "login";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Login failed: " + e.getMessage());
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password, @RequestParam String accountNumber, Model model) {
        Map<String, String> req = new HashMap<>();
        req.put("username", username);
        req.put("password", password);
        req.put("accountNumber", accountNumber);
        try {
            Map<String, Object> response = userServiceClient.register(req);
            if (response.containsKey("success") && (Boolean)response.get("success")) {
                // Create account in account-service
                Map<String, Object> accountReq = new HashMap<>();
                accountReq.put("accountNumber", accountNumber);
                accountReq.put("accountHolderName", username);
                accountReq.put("balance", 0.0);
                accountReq.put("username", username);
                try {
                    accountServiceClient.createAccount(accountReq);
                } catch (Exception ex) {
                    // Optionally log or handle account creation failure
                }
                return "redirect:/login";
            } else {
                model.addAttribute("error", response.getOrDefault("error", "Registration failed"));
                return "register";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }
}
