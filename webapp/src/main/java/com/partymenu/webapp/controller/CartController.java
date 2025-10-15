package com.partymenu.webapp.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.partymenu.webapp.entity.CartItem;
import com.partymenu.webapp.service.CartService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.partymenu.webapp.entity.User;
import com.partymenu.webapp.service.UserService;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    private Object getCurrentUserOrSessionId(HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            String email = authentication.getName();
            User user = userService.findByEmail(email).orElse(null);
            if (user != null) {
                return user.getId();
            }
        }
        return session.getId();
    }

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        Object userOrSession = getCurrentUserOrSessionId(session);
        List<CartItem> cartItems;
        Integer totalItems;
        BigDecimal totalAmount;
        String formattedTotalAmount;
        boolean isEmpty;

        if (userOrSession instanceof Long userId) {
            cartItems = cartService.getCartItemsByUserId(userId);
            totalItems = cartService.getTotalItems(userId);
            totalAmount = cartService.getTotalAmount(userId);
            formattedTotalAmount = cartService.getFormattedTotalAmount(userId);
            isEmpty = cartService.isCartEmpty(userId);
        } else {
            String sessionId = (String) userOrSession;
            cartItems = cartService.getCartItems(sessionId);
            totalItems = cartService.getTotalItems(sessionId);
            totalAmount = cartService.getTotalAmount(sessionId);
            formattedTotalAmount = cartService.getFormattedTotalAmount(sessionId);
            isEmpty = cartService.isCartEmpty(sessionId);
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("formattedTotalAmount", formattedTotalAmount);
        model.addAttribute("isEmpty", isEmpty);
        model.addAttribute("pageTitle", "Shopping Cart");

        return "cart/cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long menuItemId,
                           @RequestParam(defaultValue = "1") Integer quantity,
                           HttpSession session,
                           HttpServletRequest request,
                           RedirectAttributes redirectAttributes) {
        try {
            Object userOrSession = getCurrentUserOrSessionId(session);
            CartItem cartItem;

            if (userOrSession instanceof Long long1) {
                cartItem = cartService.addToCart(long1, menuItemId, quantity);
            } else {
                cartItem = cartService.addToCart((String) userOrSession, menuItemId, quantity);
            }

            redirectAttributes.addFlashAttribute("successMessage",
                "'" + cartItem.getMenuItem().getName() + "' added to cart successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Error adding item to cart: " + e.getMessage());
        }

        // Redirect back to the referring page, or to home if no referer
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    @PostMapping("/add-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCartAjax(@RequestParam Long menuItemId,
                                                            @RequestParam(defaultValue = "1") Integer quantity,
                                                            HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            Object userOrSession = getCurrentUserOrSessionId(session);
            CartItem cartItem;

            if (userOrSession instanceof Long long1) {
                cartItem = cartService.addToCart(long1, menuItemId, quantity);
            } else {
                cartItem = cartService.addToCart((String) userOrSession, menuItemId, quantity);
            }

            response.put("success", true);
            response.put("message", "'" + cartItem.getMenuItem().getName() + "' added to cart!");
            if (userOrSession instanceof Long long1) {
                response.put("totalItems", cartService.getTotalItems(long1));
                response.put("totalAmount", cartService.getFormattedTotalAmount(long1));
            } else {
                response.put("totalItems", cartService.getTotalItems((String) userOrSession));
                response.put("totalAmount", cartService.getFormattedTotalAmount((String) userOrSession));
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error adding item to cart: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/update/{cartItemId}")
    public String updateCartItem(@PathVariable Long cartItemId,
                                @RequestParam Integer quantity,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        try {
            Object userOrSession = getCurrentUserOrSessionId(session);
            CartItem updatedItem;

            if (userOrSession instanceof Long long1) {
                updatedItem = cartService.updateCartItemQuantity(long1, cartItemId, quantity);
            } else {
                updatedItem = cartService.updateCartItemQuantity((String) userOrSession, cartItemId, quantity);
            }

            if (updatedItem != null) {
                redirectAttributes.addFlashAttribute("successMessage",
                    "Cart item quantity updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("successMessage",
                    "Item removed from cart!");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Error updating cart item: " + e.getMessage());
        }

        return "redirect:/cart";
    }

    @PostMapping("/update-ajax/{cartItemId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCartItemAjax(@PathVariable Long cartItemId,
                                                                 @RequestParam Integer quantity,
                                                                 HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            Object userOrSession = getCurrentUserOrSessionId(session);
            CartItem updatedItem;

            if (userOrSession instanceof Long long1) {
                updatedItem = cartService.updateCartItemQuantity(long1, cartItemId, quantity);
            } else {
                updatedItem = cartService.updateCartItemQuantity((String) userOrSession, cartItemId, quantity);
            }

            response.put("success", true);
            if (updatedItem != null) {
                response.put("message", "Quantity updated successfully!");
                response.put("newTotalPrice", updatedItem.getFormattedTotalPrice());
            } else {
                response.put("message", "Item removed from cart!");
            }
            if (userOrSession instanceof Long long1) {
                response.put("totalItems", cartService.getTotalItems(long1));
                response.put("totalAmount", cartService.getFormattedTotalAmount(long1));
            } else {
                response.put("totalItems", cartService.getTotalItems((String) userOrSession));
                response.put("totalAmount", cartService.getFormattedTotalAmount((String) userOrSession));
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating cart: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/remove/{cartItemId}")
    public String removeFromCart(@PathVariable Long cartItemId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        try {
            Object userOrSession = getCurrentUserOrSessionId(session);

            if (userOrSession instanceof Long long1) {
                cartService.removeFromCart(long1, cartItemId);
            } else {
                cartService.removeFromCart((String) userOrSession, cartItemId);
            }
            redirectAttributes.addFlashAttribute("successMessage",
                "Item removed from cart successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Error removing item from cart: " + e.getMessage());
        }

        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Object userOrSession = getCurrentUserOrSessionId(session);

            if (userOrSession instanceof Long long1) {
                cartService.clearCart(long1);
            } else {
                cartService.clearCart((String) userOrSession);
            }
            redirectAttributes.addFlashAttribute("successMessage",
                "Cart cleared successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Error clearing cart: " + e.getMessage());
        }

        return "redirect:/cart";
    }

    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Integer> getCartItemCount(HttpSession session) {
        Object userOrSession = getCurrentUserOrSessionId(session);
        Integer totalItems;

        if (userOrSession instanceof Long long1) {
            totalItems = cartService.getTotalItems(long1);
        } else {
            totalItems = cartService.getTotalItems((String) userOrSession);
        }
        return ResponseEntity.ok(totalItems);
    }
}