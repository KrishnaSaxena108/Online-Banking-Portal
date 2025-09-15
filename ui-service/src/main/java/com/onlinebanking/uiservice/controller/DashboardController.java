package com.onlinebanking.uiservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/logout")
    public String logout(javax.servlet.http.HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
