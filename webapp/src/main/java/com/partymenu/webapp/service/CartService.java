package com.partymenu.webapp.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.partymenu.webapp.entity.CartItem;
import com.partymenu.webapp.entity.MenuItem;
import com.partymenu.webapp.repository.CartItemRepository;
import com.partymenu.webapp.repository.MenuItemRepository;

@Service
@Transactional
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;

    public CartService(CartItemRepository cartItemRepository, MenuItemRepository menuItemRepository) {
        this.cartItemRepository = cartItemRepository;
        this.menuItemRepository = menuItemRepository;
    }

    public List<CartItem> getCartItems(String sessionId) {
        return cartItemRepository.findBySessionIdOrderByCreatedAtDesc(sessionId);
    }

    public List<CartItem> getCartItemsByUserId(Long userId) {
        return cartItemRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public CartItem addToCart(String sessionId, Long menuItemId, Integer quantity) {
        Optional<MenuItem> menuItemOpt = menuItemRepository.findById(menuItemId);
        if (menuItemOpt.isEmpty() || !menuItemOpt.get().getIsAvailable()) {
            throw new RuntimeException("Menu item not found or not available");
        }

        MenuItem menuItem = menuItemOpt.get();

        // Check if item already exists in cart
        Optional<CartItem> existingCartItemOpt = cartItemRepository.findBySessionIdAndMenuItemId(sessionId, menuItemId);

        if (existingCartItemOpt.isPresent()) {
            // Update quantity of existing item
            CartItem existingCartItem = existingCartItemOpt.get();
            existingCartItem.setQuantity(existingCartItem.getQuantity() + quantity);
            existingCartItem.preUpdate();
            return cartItemRepository.save(existingCartItem);
        } else {
            // Add new item to cart
            CartItem newCartItem = new CartItem(sessionId, menuItem, quantity);
            return cartItemRepository.save(newCartItem);
        }
    }

    public CartItem updateCartItemQuantity(String sessionId, Long cartItemId, Integer quantity) {
        Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartItemId);
        if (cartItemOpt.isEmpty()) {
            throw new RuntimeException("Cart item not found");
        }

        CartItem cartItem = cartItemOpt.get();
        if (!cartItem.getSessionId().equals(sessionId)) {
            throw new RuntimeException("Unauthorized access to cart item");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            return null;
        } else {
            cartItem.setQuantity(quantity);
            cartItem.preUpdate();
            return cartItemRepository.save(cartItem);
        }
    }

    public void removeFromCart(String sessionId, Long cartItemId) {
        Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartItemId);
        if (cartItemOpt.isPresent()) {
            CartItem cartItem = cartItemOpt.get();
            if (cartItem.getSessionId().equals(sessionId)) {
                cartItemRepository.delete(cartItem);
            }
        }
    }

    public void clearCart(String sessionId) {
        cartItemRepository.deleteBySessionId(sessionId);
    }

    public Integer getTotalItems(String sessionId) {
        Integer total = cartItemRepository.getTotalItemsBySession(sessionId);
        return total != null ? total : 0;
    }

    public BigDecimal getTotalAmount(String sessionId) {
        BigDecimal total = cartItemRepository.getTotalAmountBySession(sessionId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public String getFormattedTotalAmount(String sessionId) {
        BigDecimal total = getTotalAmount(sessionId);
        return String.format("$%.2f", total);
    }

    public boolean isCartEmpty(String sessionId) {
        return getCartItems(sessionId).isEmpty();
    }

    // Cleanup old cart items (can be called by a scheduled job)
    public void cleanupOldCartItems(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        cartItemRepository.deleteOldCartItems(cutoffDate);
    }

    // User-based cart methods
    public CartItem addToCart(Long userId, Long menuItemId, Integer quantity) {
        Optional<MenuItem> menuItemOpt = menuItemRepository.findById(menuItemId);
        if (menuItemOpt.isEmpty() || !menuItemOpt.get().getIsAvailable()) {
            throw new RuntimeException("Menu item not found or not available");
        }

        MenuItem menuItem = menuItemOpt.get();

        // Check if item already exists in cart
        Optional<CartItem> existingCartItemOpt = cartItemRepository.findByUserIdAndMenuItemId(userId, menuItemId);

        if (existingCartItemOpt.isPresent()) {
            // Update quantity of existing item
            CartItem existingCartItem = existingCartItemOpt.get();
            existingCartItem.setQuantity(existingCartItem.getQuantity() + quantity);
            existingCartItem.preUpdate();
            return cartItemRepository.save(existingCartItem);
        } else {
            // Add new item to cart
            CartItem newCartItem = new CartItem(userId, menuItem, quantity);
            return cartItemRepository.save(newCartItem);
        }
    }

    public CartItem updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity) {
        Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartItemId);
        if (cartItemOpt.isEmpty()) {
            throw new RuntimeException("Cart item not found");
        }

        CartItem cartItem = cartItemOpt.get();
        if (!cartItem.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to cart item");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            return null;
        } else {
            cartItem.setQuantity(quantity);
            cartItem.preUpdate();
            return cartItemRepository.save(cartItem);
        }
    }

    public void removeFromCart(Long userId, Long cartItemId) {
        Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartItemId);
        if (cartItemOpt.isPresent()) {
            CartItem cartItem = cartItemOpt.get();
            if (cartItem.getUserId() != null && cartItem.getUserId().equals(userId)) {
                cartItemRepository.delete(cartItem);
            }
        }
    }

    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    public Integer getTotalItems(Long userId) {
        Integer total = cartItemRepository.getTotalItemsByUser(userId);
        return total != null ? total : 0;
    }

    public BigDecimal getTotalAmount(Long userId) {
        BigDecimal total = cartItemRepository.getTotalAmountByUser(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public String getFormattedTotalAmount(Long userId) {
        BigDecimal total = getTotalAmount(userId);
        return String.format("$%.2f", total);
    }

    public boolean isCartEmpty(Long userId) {
        return getCartItemsByUserId(userId).isEmpty();
    }

    public void migrateSessionCartToUser(String sessionId, Long userId) {
        cartItemRepository.migrateSessionCartToUser(sessionId, userId);
    }
}
