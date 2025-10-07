package com.partymenu.webapp.repository;

import com.partymenu.webapp.entity.Category;
import com.partymenu.webapp.entity.MenuType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Find categories by menu type
    List<Category> findByMenuTypeAndIsActiveTrueOrderByDisplayOrderAsc(MenuType menuType);
    
    // Find categories by menu type ID
    List<Category> findByMenuType_IdAndIsActiveTrueOrderByDisplayOrderAsc(Long menuTypeId);
    
    // Find active categories
    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();
    
    // Find category by name and menu type
    Optional<Category> findByNameIgnoreCaseAndMenuType(String name, MenuType menuType);
    
    // Check if category exists
    boolean existsByNameIgnoreCaseAndMenuType(String name, MenuType menuType);
    
    // Custom query to find categories with menu items
    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.menuItems mi " +
           "WHERE c.menuType.id = :menuTypeId AND c.isActive = true " +
           "ORDER BY c.displayOrder, c.name")
    List<Category> findCategoriesWithMenuItemsByMenuType(@Param("menuTypeId") Long menuTypeId);
    
    // Find categories with available menu items
    @Query("SELECT DISTINCT c FROM Category c JOIN c.menuItems mi " +
           "WHERE c.isActive = true AND mi.isAvailable = true " +
           "ORDER BY c.displayOrder")
    List<Category> findCategoriesWithAvailableMenuItems();
    
    // Count categories by menu type
    long countByMenuTypeAndIsActiveTrue(MenuType menuType);
}
