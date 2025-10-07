package com.partymenu.webapp.repository;

import com.partymenu.webapp.entity.Category;
import com.partymenu.webapp.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    
    // Find menu items by category
    List<MenuItem> findByCategoryAndIsAvailableTrueOrderByNameAsc(Category category);
    
    // Find menu items by category ID
    List<MenuItem> findByCategory_IdAndIsAvailableTrueOrderByNameAsc(Long categoryId);
    
    // Find popular menu items
    List<MenuItem> findByIsPopularTrueAndIsAvailableTrueOrderByNameAsc();
    
    // Find available menu items
    List<MenuItem> findByIsAvailableTrueOrderByNameAsc();
    
    // Search menu items by name
    List<MenuItem> findByNameContainingIgnoreCaseAndIsAvailableTrueOrderByNameAsc(String name);
    
    // Find menu items by price range
    List<MenuItem> findByPriceBetweenAndIsAvailableTrueOrderByPriceAsc(BigDecimal minPrice, BigDecimal maxPrice);
    
    // Find menu items by menu type through category
    @Query("SELECT mi FROM MenuItem mi JOIN mi.category c JOIN c.menuType mt " +
           "WHERE mt.id = :menuTypeId AND mi.isAvailable = true " +
           "ORDER BY c.displayOrder, mi.name")
    List<MenuItem> findAvailableMenuItemsByMenuType(@Param("menuTypeId") Long menuTypeId);
    
    // Search across all fields
    @Query("SELECT mi FROM MenuItem mi WHERE mi.isAvailable = true AND " +
           "(LOWER(mi.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(mi.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY mi.name")
    List<MenuItem> searchMenuItems(@Param("keyword") String keyword);
    
    // Find random popular items for homepage
    @Query(value = "SELECT * FROM menu_items WHERE is_popular = true AND is_available = true " +
                   "ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<MenuItem> findRandomPopularItems(@Param("limit") int limit);
    
    // Count menu items by category
    long countByCategoryAndIsAvailableTrue(Category category);
    
    // Find cheapest items in category
    @Query("SELECT mi FROM MenuItem mi WHERE mi.category = :category AND mi.isAvailable = true " +
           "ORDER BY mi.price ASC")
    List<MenuItem> findCheapestItemsInCategory(@Param("category") Category category);
    
    // Get price statistics
    @Query("SELECT MIN(mi.price), MAX(mi.price), AVG(mi.price) FROM MenuItem mi WHERE mi.isAvailable = true")
    Object[] getPriceStatistics();
}
