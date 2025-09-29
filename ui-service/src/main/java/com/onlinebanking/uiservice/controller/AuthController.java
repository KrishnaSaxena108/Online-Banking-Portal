package com.onlinebanking.uiservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String username, 
                           @RequestParam String password,
                           HttpSession session, 
                           Model model) {
        try {
            // Create login request
            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("username", username);
            loginRequest.put("password", password);

            // Call user service for authentication
            String userServiceUrl = "http://localhost:8084/api/login";
            ResponseEntity<Map> response = restTemplate.postForEntity(userServiceUrl, loginRequest, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("token")) {
                    // Login successful - user service returns JWT token
                    session.setAttribute("username", username);
                    session.setAttribute("authenticated", true);
                    session.setAttribute("token", responseBody.get("token")); // Store JWT token
                    return "redirect:/dashboard";
                } else if (responseBody != null && responseBody.containsKey("error")) {
                    // Specific error from user service
                    String errorMsg = (String) responseBody.get("error");
                    if (errorMsg.toLowerCase().contains("password")) {
                        model.addAttribute("error", "❌ Incorrect password. Please check your password and try again.");
                    } else if (errorMsg.toLowerCase().contains("username") || errorMsg.toLowerCase().contains("user")) {
                        model.addAttribute("error", "❌ Username not found. Please check your username or create a new account.");
                    } else {
                        model.addAttribute("error", "❌ " + errorMsg);
                    }
                    return "login";
                }
            } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                model.addAttribute("error", "❌ Invalid credentials. Please check your username and password.");
                return "login";
            }
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("401")) {
                model.addAttribute("error", "❌ Invalid username or password. Please try again.");
            } else if (e.getMessage() != null && e.getMessage().contains("Connection")) {
                model.addAttribute("error", "❌ Service temporarily unavailable. Please try again later.");
            } else {
                model.addAttribute("error", "❌ Login failed. Please check your credentials and try again.");
            }
            return "login";
        }
        
        model.addAttribute("error", "❌ Invalid credentials. Please verify your username and password.");
        return "login";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username, 
                              @RequestParam String password,
                              @RequestParam String accountNumber,
                              Model model) {
        try {
            // Create registration request
            Map<String, String> registerRequest = new HashMap<>();
            registerRequest.put("username", username);
            registerRequest.put("password", password);
            registerRequest.put("accountNumber", accountNumber);

            // Call user service for registration
            String userServiceUrl = "http://localhost:8084/api/register";
            ResponseEntity<Map> response = restTemplate.postForEntity(userServiceUrl, registerRequest, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("success") && (Boolean) responseBody.get("success")) {
                    model.addAttribute("success", "✅ Registration successful! Please login with your credentials.");
                    return "login";
                } else if (responseBody != null && responseBody.containsKey("error")) {
                    // Specific error from user service
                    String errorMsg = (String) responseBody.get("error");
                    if (errorMsg.toLowerCase().contains("username") && errorMsg.toLowerCase().contains("exist")) {
                        model.addAttribute("error", "❌ Username already exists. Please choose a different username.");
                    } else if (errorMsg.toLowerCase().contains("account") && errorMsg.toLowerCase().contains("exist")) {
                        model.addAttribute("error", "❌ Account number already exists. Please use a different account number.");
                    } else if (errorMsg.toLowerCase().contains("invalid")) {
                        model.addAttribute("error", "❌ Invalid input. Please check your information and try again.");
                    } else {
                        model.addAttribute("error", "❌ " + errorMsg);
                    }
                    return "register";
                }
            } else if (response.getStatusCode() == HttpStatus.CONFLICT) {
                model.addAttribute("error", "❌ Account already exists. Please use different credentials.");
                return "register";
            }
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("409")) {
                model.addAttribute("error", "❌ Username or account number already exists. Please try different values.");
            } else if (e.getMessage() != null && e.getMessage().contains("400")) {
                model.addAttribute("error", "❌ Invalid information provided. Please check your input and try again.");
            } else if (e.getMessage() != null && e.getMessage().contains("Connection")) {
                model.addAttribute("error", "❌ Service temporarily unavailable. Please try again later.");
            } else {
                model.addAttribute("error", "❌ Registration failed. Please check your information and try again.");
            }
            return "register";
        }
        
        model.addAttribute("error", "❌ Registration failed. Please verify your information and try again.");
        return "register";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}