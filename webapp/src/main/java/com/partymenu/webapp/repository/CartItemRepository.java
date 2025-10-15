package com.partymenu.webapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.partymenu.webapp.entity.CartItem;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findBySessionIdOrderByCreatedAtDesc(String sessionId);

    Optional<CartItem> findBySessionIdAndMenuItemId(String sessionId, Long menuItemId);

    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.sessionId = :sessionId")
    Integer getTotalItemsBySession(@Param("sessionId") String sessionId);

    @Query("SELECT SUM(ci.totalPrice) FROM CartItem ci WHERE ci.sessionId = :sessionId")
    java.math.BigDecimal getTotalAmountBySession(@Param("sessionId") String sessionId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.sessionId = :sessionId")
    void deleteBySessionId(@Param("sessionId") String sessionId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.createdAt < :cutoffDate")
    void deleteOldCartItems(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);

    // User-based queries
    List<CartItem> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<CartItem> findByUserIdAndMenuItemId(Long userId, Long menuItemId);

    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.userId = :userId")
    Integer getTotalItemsByUser(@Param("userId") Long userId);

    @Query("SELECT SUM(ci.totalPrice) FROM CartItem ci WHERE ci.userId = :userId")
    java.math.BigDecimal getTotalAmountByUser(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    // Migrate session cart to user cart
    @Modifying
    @Query("UPDATE CartItem ci SET ci.userId = :userId, ci.sessionId = '' WHERE ci.sessionId = :sessionId")
    void migrateSessionCartToUser(@Param("sessionId") String sessionId, @Param("userId") Long userId);
}