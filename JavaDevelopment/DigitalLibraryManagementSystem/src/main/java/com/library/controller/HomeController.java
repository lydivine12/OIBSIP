package com.library.controller;

import com.library.model.Role;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import static com.library.config.AuthInterceptor.SESSION_MEMBER_ID;
import static com.library.config.AuthInterceptor.SESSION_ROLE;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpSession session) {
        Object memberId = session.getAttribute(SESSION_MEMBER_ID);
        if (memberId == null) {
            return "redirect:/login";
        }
        Object role = session.getAttribute(SESSION_ROLE);
        return role == Role.ADMIN ? "redirect:/admin/dashboard" : "redirect:/user/catalogue";
    }
}
