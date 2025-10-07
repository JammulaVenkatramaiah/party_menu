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
}