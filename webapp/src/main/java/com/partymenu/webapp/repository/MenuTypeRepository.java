package com.partymenu.webapp.repository;

import com.partymenu.webapp.entity.MenuType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuTypeRepository extends JpaRepository<MenuType, Long> {
    
    // Find active menu types
    List<MenuType> findByIsActiveTrueOrderByNameAsc();
    
    // Find menu type by name
    Optional<MenuType> findByNameIgnoreCase(String name);
    
    // Check if menu type exists by name
    boolean existsByNameIgnoreCase(String name);
    
    // Custom query to find menu types with categories
    @Query("SELECT DISTINCT mt FROM MenuType mt LEFT JOIN FETCH mt.categories c WHERE mt.isActive = true ORDER BY mt.name")
    List<MenuType> findActiveMenuTypesWithCategories();
    
    // Count active menu types
    long countByIsActiveTrue();
}
