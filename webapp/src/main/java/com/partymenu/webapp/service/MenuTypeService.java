package com.partymenu.webapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.partymenu.webapp.entity.MenuType;
import com.partymenu.webapp.repository.MenuTypeRepository;

@Service
@Transactional
public class MenuTypeService {
    
    private final MenuTypeRepository menuTypeRepository;
    
    @Autowired
    public MenuTypeService(MenuTypeRepository menuTypeRepository) {
        this.menuTypeRepository = menuTypeRepository;
    }
    
    // Get all active menu types
    @Transactional(readOnly = true)
    public List<MenuType> getAllActiveMenuTypes() {
        return menuTypeRepository.findByIsActiveTrueOrderByNameAsc();
    }
    
    // Get all menu types
    @Transactional(readOnly = true)
    public List<MenuType> getAllMenuTypes() {
        return menuTypeRepository.findAll();
    }
    
    // Get menu type by ID
    @Transactional(readOnly = true)
    public Optional<MenuType> getMenuTypeById(Long id) {
        return menuTypeRepository.findById(id);
    }
    
    // Get menu type by name
    @Transactional(readOnly = true)
    public Optional<MenuType> getMenuTypeByName(String name) {
        return menuTypeRepository.findByNameIgnoreCase(name);
    }
    
    // Save menu type
    public MenuType saveMenuType(MenuType menuType) {
        return menuTypeRepository.save(menuType);
    }
    
    // Create new menu type
    public MenuType createMenuType(String name, String description) {
        if (menuTypeRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Menu type with name '" + name + "' already exists");
        }
        
        MenuType menuType = new MenuType(name, description);
        return menuTypeRepository.save(menuType);
    }
    
    // Update menu type
    public MenuType updateMenuType(Long id, String name, String description) {
        MenuType menuType = menuTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu type not found with id: " + id));
        
        // Check if name is already taken by another menu type
        Optional<MenuType> existingMenuType = menuTypeRepository.findByNameIgnoreCase(name);
        if (existingMenuType.isPresent() && !existingMenuType.get().getId().equals(id)) {
            throw new IllegalArgumentException("Menu type with name '" + name + "' already exists");
        }
        
        menuType.setName(name);
        menuType.setDescription(description);
        return menuTypeRepository.save(menuType);
    }
    
    // Delete menu type
    public void deleteMenuType(Long id) {
        MenuType menuType = menuTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu type not found with id: " + id));
        
        menuTypeRepository.delete(menuType);
    }
    
    // Toggle active status
    public MenuType toggleActiveStatus(Long id) {
        MenuType menuType = menuTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu type not found with id: " + id));
        
        menuType.setIsActive(!menuType.getIsActive());
        return menuTypeRepository.save(menuType);
    }
    
    // Check if menu type exists by name
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return menuTypeRepository.existsByNameIgnoreCase(name);
    }
    
    // Get menu types with categories
    @Transactional(readOnly = true)
    public List<MenuType> getActiveMenuTypesWithCategories() {
        return menuTypeRepository.findActiveMenuTypesWithCategories();
    }
    
    // Get count of active menu types
    @Transactional(readOnly = true)
    public long getActiveMenuTypeCount() {
        return menuTypeRepository.countByIsActiveTrue();
    }
}
