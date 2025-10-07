package com.partymenu.webapp.service;

import com.partymenu.webapp.entity.Category;
import com.partymenu.webapp.entity.MenuItem;
import com.partymenu.webapp.repository.CategoryRepository;
import com.partymenu.webapp.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MenuItemService {
    
    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    
    @Autowired
    public MenuItemService(MenuItemRepository menuItemRepository, CategoryRepository categoryRepository) {
        this.menuItemRepository = menuItemRepository;
        this.categoryRepository = categoryRepository;
    }
    
    // Get all available menu items
    @Transactional(readOnly = true)
    public List<MenuItem> getAllAvailableMenuItems() {
        return menuItemRepository.findByIsAvailableTrueOrderByNameAsc();
    }
    
    // Get menu items by category
    @Transactional(readOnly = true)
    public List<MenuItem> getMenuItemsByCategory(Long categoryId) {
        return menuItemRepository.findByCategory_IdAndIsAvailableTrueOrderByNameAsc(categoryId);
    }
    
    // Get menu items by category entity
    @Transactional(readOnly = true)
    public List<MenuItem> getMenuItemsByCategory(Category category) {
        return menuItemRepository.findByCategoryAndIsAvailableTrueOrderByNameAsc(category);
    }
    
    // Get menu item by ID
    @Transactional(readOnly = true)
    public Optional<MenuItem> getMenuItemById(Long id) {
        return menuItemRepository.findById(id);
    }
    
    // Get all menu items
    @Transactional(readOnly = true)
    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }
    
    // Get popular menu items
    @Transactional(readOnly = true)
    public List<MenuItem> getPopularMenuItems() {
        return menuItemRepository.findByIsPopularTrueAndIsAvailableTrueOrderByNameAsc();
    }
    
    // Save menu item
    public MenuItem saveMenuItem(MenuItem menuItem) {
        return menuItemRepository.save(menuItem);
    }
    
    // Create new menu item
    public MenuItem createMenuItem(String name, String description, BigDecimal price, 
                                  String imageUrl, Long categoryId, Boolean isPopular, 
                                  Boolean isAvailable, Integer preparationTime) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));
        
        MenuItem menuItem = new MenuItem(name, description, price, category);
        menuItem.setImageUrl(imageUrl);
        
        if (isPopular != null) {
            menuItem.setIsPopular(isPopular);
        }
        if (isAvailable != null) {
            menuItem.setIsAvailable(isAvailable);
        }
        if (preparationTime != null) {
            menuItem.setPreparationTime(preparationTime);
        }
        
        return menuItemRepository.save(menuItem);
    }
    
    // Update menu item
    public MenuItem updateMenuItem(Long id, String name, String description, BigDecimal price,
                                  String imageUrl, Long categoryId, Boolean isPopular,
                                  Boolean isAvailable, Integer preparationTime) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found with id: " + id));
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));
        
        menuItem.setName(name);
        menuItem.setDescription(description);
        menuItem.setPrice(price);
        menuItem.setImageUrl(imageUrl);
        menuItem.setCategory(category);
        
        if (isPopular != null) {
            menuItem.setIsPopular(isPopular);
        }
        if (isAvailable != null) {
            menuItem.setIsAvailable(isAvailable);
        }
        if (preparationTime != null) {
            menuItem.setPreparationTime(preparationTime);
        }
        
        return menuItemRepository.save(menuItem);
    }
    
    // Delete menu item
    public void deleteMenuItem(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found with id: " + id));
        
        menuItemRepository.delete(menuItem);
    }
    
    // Toggle availability
    public MenuItem toggleAvailability(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found with id: " + id));
        
        menuItem.setIsAvailable(!menuItem.getIsAvailable());
        return menuItemRepository.save(menuItem);
    }
    
    // Toggle popular status
    public MenuItem togglePopularStatus(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found with id: " + id));
        
        menuItem.setIsPopular(!menuItem.getIsPopular());
        return menuItemRepository.save(menuItem);
    }
    
    // Search menu items
    @Transactional(readOnly = true)
    public List<MenuItem> searchMenuItems(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllAvailableMenuItems();
        }
        return menuItemRepository.searchMenuItems(keyword.trim());
    }
    
    // Search by name only
    @Transactional(readOnly = true)
    public List<MenuItem> searchByName(String name) {
        return menuItemRepository.findByNameContainingIgnoreCaseAndIsAvailableTrueOrderByNameAsc(name);
    }
    
    // Find menu items by price range
    @Transactional(readOnly = true)
    public List<MenuItem> getMenuItemsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return menuItemRepository.findByPriceBetweenAndIsAvailableTrueOrderByPriceAsc(minPrice, maxPrice);
    }
    
    // Get menu items by menu type
    @Transactional(readOnly = true)
    public List<MenuItem> getMenuItemsByMenuType(Long menuTypeId) {
        return menuItemRepository.findAvailableMenuItemsByMenuType(menuTypeId);
    }
    
    // Get random popular items for homepage
    @Transactional(readOnly = true)
    public List<MenuItem> getRandomPopularItems(int limit) {
        return menuItemRepository.findRandomPopularItems(limit);
    }
    
    // Get count of menu items by category
    @Transactional(readOnly = true)
    public long getMenuItemCountByCategory(Category category) {
        return menuItemRepository.countByCategoryAndIsAvailableTrue(category);
    }
    
    // Get cheapest items in category
    @Transactional(readOnly = true)
    public List<MenuItem> getCheapestItemsInCategory(Category category) {
        return menuItemRepository.findCheapestItemsInCategory(category);
    }
    
    // Get price statistics
    @Transactional(readOnly = true)
    public Object[] getPriceStatistics() {
        return menuItemRepository.getPriceStatistics();
    }
}
