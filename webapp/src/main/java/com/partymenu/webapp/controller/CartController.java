package com.partymenu.webapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    private String getSessionId(HttpSession session) {
        return session.getId();
    }

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        String sessionId = getSessionId(session);
        List<CartItem> cartItems = cartService.getCartItems(sessionId);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalItems", cartService.getTotalItems(sessionId));
        model.addAttribute("totalAmount", cartService.getTotalAmount(sessionId));
        model.addAttribute("formattedTotalAmount", cartService.getFormattedTotalAmount(sessionId));
        model.addAttribute("isEmpty", cartService.isCartEmpty(sessionId));
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
            String sessionId = getSessionId(session);
            CartItem cartItem = cartService.addToCart(sessionId, menuItemId, quantity);

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
            String sessionId = getSessionId(session);
            CartItem cartItem = cartService.addToCart(sessionId, menuItemId, quantity);

            response.put("success", true);
            response.put("message", "'" + cartItem.getMenuItem().getName() + "' added to cart!");
            response.put("totalItems", cartService.getTotalItems(sessionId));
            response.put("totalAmount", cartService.getFormattedTotalAmount(sessionId));

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
            String sessionId = getSessionId(session);
            CartItem updatedItem = cartService.updateCartItemQuantity(sessionId, cartItemId, quantity);

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
            String sessionId = getSessionId(session);
            CartItem updatedItem = cartService.updateCartItemQuantity(sessionId, cartItemId, quantity);

            response.put("success", true);
            if (updatedItem != null) {
                response.put("message", "Quantity updated successfully!");
                response.put("newTotalPrice", updatedItem.getFormattedTotalPrice());
            } else {
                response.put("message", "Item removed from cart!");
            }
            response.put("totalItems", cartService.getTotalItems(sessionId));
            response.put("totalAmount", cartService.getFormattedTotalAmount(sessionId));

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
            String sessionId = getSessionId(session);
            cartService.removeFromCart(sessionId, cartItemId);
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
            String sessionId = getSessionId(session);
            cartService.clearCart(sessionId);
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
        String sessionId = getSessionId(session);
        return ResponseEntity.ok(cartService.getTotalItems(sessionId));
    }
}