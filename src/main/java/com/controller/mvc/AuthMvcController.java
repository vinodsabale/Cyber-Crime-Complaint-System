package com.controller.mvc;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dto.request.RegisterRequest;
import com.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
 
@Controller
@RequiredArgsConstructor
public class AuthMvcController {
 
    private final UserService userService;
 
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model) {
        if (error  != null) model.addAttribute("error",   "Invalid email or password");
        if (logout != null) model.addAttribute("message", "Logged out successfully");
        return "auth/login";
    }

    @GetMapping("/logout-success")
    public String logoutSuccess() {
        return "auth/logout";
    }
 
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new RegisterRequest());
        return "auth/register";
    }
 
    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("user") RegisterRequest req,
            BindingResult result,
            RedirectAttributes ra,
            Model model) {
        if (result.hasErrors()) return "auth/register";
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            model.addAttribute("error", "Passwords do not match");
            return "auth/register";
        }
        try {
            userService.register(req);
            ra.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }
}
 