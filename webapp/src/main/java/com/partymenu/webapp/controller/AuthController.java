 package com.partymenu.webapp.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.partymenu.webapp.entity.CartItem;
import com.partymenu.webapp.entity.User;
import com.partymenu.webapp.service.CartService;
import com.partymenu.webapp.service.UserService;



@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final CartService cartService;

    public AuthController(UserService userService, CartService cartService) {
        this.userService = userService;
        this.cartService = cartService;
    }



    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("pageTitle", "Login - Party Menu");
        return "login";
    }

    // Note: Logout is handled by Spring Security, this method is not used

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("pageTitle", "Register - Party Menu");
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Register - Party Menu");
            return "register";
        }

        try {
            userService.registerUser(user.getName(), user.getPhone(), user.getEmail(), user.getPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please log in.");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "Register - Party Menu");
            return "register";
        }
    }

    @GetMapping("/profile")
    public String showProfile(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/";
        }

        String email = authentication.getName();
        User user = userService.findByEmail(email).orElseThrow();

        // Get cart statistics
        int totalItems = cartService.getTotalItems(user.getId());
        double totalAmount = cartService.getTotalAmount(user.getId()).doubleValue();
        List<CartItem> cartItems = cartService.getCartItemsByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("cartStats", Map.of(
            "totalItems", totalItems,
            "totalAmount", totalAmount,
            "cartItems", cartItems
        ));
        model.addAttribute("pageTitle", "Profile - Party Menu");
        return "profile";
    }
}
