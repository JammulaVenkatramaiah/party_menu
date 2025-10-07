package com.partymenu.webapp.controller;

import com.partymenu.webapp.entity.Category;
import com.partymenu.webapp.entity.MenuItem;
import com.partymenu.webapp.service.CategoryService;
import com.partymenu.webapp.service.MenuItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/menu-items")
public class MenuItemController {
    
    private final MenuItemService menuItemService;
    private final CategoryService categoryService;
    
    @Autowired
    public MenuItemController(MenuItemService menuItemService, CategoryService categoryService) {
        this.menuItemService = menuItemService;
        this.categoryService = categoryService;
    }
    
    @GetMapping
    public String listMenuItems(Model model) {
        List<MenuItem> menuItems = menuItemService.getAllMenuItems();
        model.addAttribute("menuItems", menuItems);
        model.addAttribute("pageTitle", "Manage Menu Items");
        return "admin/menu-items/list";
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        MenuItem menuItem = new MenuItem();
        menuItem.setIsAvailable(true);
        menuItem.setIsPopular(false);
        
        List<Category> categories = categoryService.getAllActiveCategories();
        
        model.addAttribute("menuItem", menuItem);
        model.addAttribute("categories", categories);
        model.addAttribute("pageTitle", "Add New Menu Item");
        
        return "admin/menu-items/form";
    }
    
    @PostMapping("/save")
    public String saveMenuItem(@Valid @ModelAttribute MenuItem menuItem, 
                              BindingResult result, 
                              Model model, 
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            List<Category> categories = categoryService.getAllActiveCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("pageTitle", menuItem.getId() == null ? "Add New Menu Item" : "Edit Menu Item");
            return "admin/menu-items/form";
        }
        
        try {
            menuItemService.saveMenuItem(menuItem);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Menu item '" + menuItem.getName() + "' has been saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error saving menu item: " + e.getMessage());
        }
        
        return "redirect:/admin/menu-items";
    }
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<MenuItem> menuItemOpt = menuItemService.getMenuItemById(id);
        if (menuItemOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Menu item not found!");
            return "redirect:/admin/menu-items";
        }
        
        List<Category> categories = categoryService.getAllActiveCategories();
        
        model.addAttribute("menuItem", menuItemOpt.get());
        model.addAttribute("categories", categories);
        model.addAttribute("pageTitle", "Edit Menu Item");
        
        return "admin/menu-items/form";
    }
    
    @GetMapping("/delete/{id}")
    public String deleteMenuItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<MenuItem> menuItemOpt = menuItemService.getMenuItemById(id);
            if (menuItemOpt.isPresent()) {
                menuItemService.deleteMenuItem(id);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Menu item '" + menuItemOpt.get().getName() + "' has been deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Menu item not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error deleting menu item: " + e.getMessage());
        }
        
        return "redirect:/admin/menu-items";
    }
    
    @GetMapping("/toggle-availability/{id}")
    public String toggleAvailability(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            MenuItem menuItem = menuItemService.toggleAvailability(id);
            String status = menuItem.getIsAvailable() ? "available" : "unavailable";
            redirectAttributes.addFlashAttribute("successMessage", 
                "Menu item '" + menuItem.getName() + "' is now " + status + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error updating menu item availability: " + e.getMessage());
        }
        
        return "redirect:/admin/menu-items";
    }
    
    @GetMapping("/toggle-popular/{id}")
    public String togglePopularStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            MenuItem menuItem = menuItemService.togglePopularStatus(id);
            String status = menuItem.getIsPopular() ? "popular" : "regular";
            redirectAttributes.addFlashAttribute("successMessage", 
                "Menu item '" + menuItem.getName() + "' is now marked as " + status + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error updating menu item status: " + e.getMessage());
        }
        
        return "redirect:/admin/menu-items";
    }
}
