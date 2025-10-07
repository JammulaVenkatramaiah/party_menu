package com.partymenu.webapp.service;

import com.partymenu.webapp.entity.Category;
import com.partymenu.webapp.entity.MenuType;
import com.partymenu.webapp.repository.CategoryRepository;
import com.partymenu.webapp.repository.MenuTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final MenuTypeRepository menuTypeRepository;
    
    @Autowired
    public CategoryService(CategoryRepository categoryRepository, MenuTypeRepository menuTypeRepository) {
        this.categoryRepository = categoryRepository;
        this.menuTypeRepository = menuTypeRepository;
    }
    
    // Get all active categories
    @Transactional(readOnly = true)
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }
    
    // Get categories by menu type
    @Transactional(readOnly = true)
    public List<Category> getCategoriesByMenuType(Long menuTypeId) {
        return categoryRepository.findByMenuType_IdAndIsActiveTrueOrderByDisplayOrderAsc(menuTypeId);
    }
    
    // Get categories by menu type entity
    @Transactional(readOnly = true)
    public List<Category> getCategoriesByMenuType(MenuType menuType) {
        return categoryRepository.findByMenuTypeAndIsActiveTrueOrderByDisplayOrderAsc(menuType);
    }
    
    // Get category by ID
    @Transactional(readOnly = true)
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }
    
    // Get all categories
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    // Save category
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }
    
    // Create new category
    public Category createCategory(String name, String description, Long menuTypeId, Integer displayOrder) {
        MenuType menuType = menuTypeRepository.findById(menuTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Menu type not found with id: " + menuTypeId));
        
        if (categoryRepository.existsByNameIgnoreCaseAndMenuType(name, menuType)) {
            throw new IllegalArgumentException("Category with name '" + name + "' already exists in this menu type");
        }
        
        Category category = new Category(name, description, menuType);
        if (displayOrder != null) {
            category.setDisplayOrder(displayOrder);
        }
        return categoryRepository.save(category);
    }
    
    // Update category
    public Category updateCategory(Long id, String name, String description, Long menuTypeId, Integer displayOrder) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
        
        MenuType menuType = menuTypeRepository.findById(menuTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Menu type not found with id: " + menuTypeId));
        
        // Check if name is already taken by another category in the same menu type
        Optional<Category> existingCategory = categoryRepository.findByNameIgnoreCaseAndMenuType(name, menuType);
        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(id)) {
            throw new IllegalArgumentException("Category with name '" + name + "' already exists in this menu type");
        }
        
        category.setName(name);
        category.setDescription(description);
        category.setMenuType(menuType);
        if (displayOrder != null) {
            category.setDisplayOrder(displayOrder);
        }
        return categoryRepository.save(category);
    }
    
    // Delete category
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
        
        categoryRepository.delete(category);
    }
    
    // Toggle active status
    public Category toggleActiveStatus(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
        
        category.setIsActive(!category.getIsActive());
        return categoryRepository.save(category);
    }
    
    // Get categories with menu items
    @Transactional(readOnly = true)
    public List<Category> getCategoriesWithMenuItemsByMenuType(Long menuTypeId) {
        return categoryRepository.findCategoriesWithMenuItemsByMenuType(menuTypeId);
    }
    
    // Get categories with available menu items
    @Transactional(readOnly = true)
    public List<Category> getCategoriesWithAvailableMenuItems() {
        return categoryRepository.findCategoriesWithAvailableMenuItems();
    }
    
    // Check if category exists by name and menu type
    @Transactional(readOnly = true)
    public boolean existsByNameAndMenuType(String name, MenuType menuType) {
        return categoryRepository.existsByNameIgnoreCaseAndMenuType(name, menuType);
    }
    
    // Get count of categories by menu type
    @Transactional(readOnly = true)
    public long getCategoryCountByMenuType(MenuType menuType) {
        return categoryRepository.countByMenuTypeAndIsActiveTrue(menuType);
    }
}
