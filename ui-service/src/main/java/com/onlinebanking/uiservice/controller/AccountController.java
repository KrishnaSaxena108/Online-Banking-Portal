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
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.client.RestTemplate;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

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
}
