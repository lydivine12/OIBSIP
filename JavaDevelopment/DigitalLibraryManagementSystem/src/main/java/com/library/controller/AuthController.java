package com.library.controller;

import com.library.model.Member;
import com.library.model.Role;
import com.library.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

import static com.library.config.AuthInterceptor.*;

@Controller
public class AuthController {

    private final MemberService memberService;

    public AuthController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                         @RequestParam String password,
                         HttpSession session,
                         Model model) {
        Optional<Member> member = memberService.authenticate(email, password);
        if (member.isEmpty()) {
            model.addAttribute("error", "Invalid email or password.");
            return "login";
        }
        Member m = member.get();
        session.setAttribute(SESSION_MEMBER_ID, m.getId());
        session.setAttribute(SESSION_ROLE, m.getRole());
        session.setAttribute(SESSION_NAME, m.getFullName());
        return m.getRole() == Role.ADMIN ? "redirect:/admin/dashboard" : "redirect:/user/catalogue";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                            @RequestParam String email,
                            @RequestParam String password,
                            @RequestParam String confirmPassword,
                            Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "register";
        }
        try {
            memberService.register(fullName, email, password);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
        return "redirect:/login?registered";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
