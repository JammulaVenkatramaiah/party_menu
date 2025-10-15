package com.partymenu.webapp.controller;

import com.partymenu.webapp.entity.Category;
import com.partymenu.webapp.entity.MenuItem;
import com.partymenu.webapp.entity.MenuType;
import com.partymenu.webapp.service.CategoryService;
import com.partymenu.webapp.service.MenuItemService;
import com.partymenu.webapp.service.MenuTypeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/menu")
public class MenuController {
    
    private final MenuTypeService menuTypeService;
    private final CategoryService categoryService;
    private final MenuItemService menuItemService;
    
    public MenuController(MenuTypeService menuTypeService, CategoryService categoryService, MenuItemService menuItemService) {
        this.menuTypeService = menuTypeService;
        this.categoryService = categoryService;
        this.menuItemService = menuItemService;
    }
    
    @GetMapping
    public String menuHome(Model model) {
        List<MenuType> menuTypes = menuTypeService.getAllActiveMenuTypes();
        model.addAttribute("menuTypes", menuTypes);
        model.addAttribute("pageTitle", "Our Party Menus");
        return "menu/menu-list";
    }
    
    @GetMapping("/{menuTypeId}")
    public String viewMenuByType(@PathVariable Long menuTypeId, Model model) {
        Optional<MenuType> menuTypeOpt = menuTypeService.getMenuTypeById(menuTypeId);
        if (menuTypeOpt.isEmpty()) {
            return "redirect:/menu";
        }
        
        MenuType menuType = menuTypeOpt.get();
        List<Category> categories = categoryService.getCategoriesWithMenuItemsByMenuType(menuTypeId);
        
        model.addAttribute("menuType", menuType);
        model.addAttribute("categories", categories);
        model.addAttribute("pageTitle", menuType.getName() + " Menu");
        
        return "menu/menu-detail";
    }
    
    @GetMapping("/category/{categoryId}")
    public String viewMenuByCategory(@PathVariable Long categoryId, Model model) {
        Optional<Category> categoryOpt = categoryService.getCategoryById(categoryId);
        if (categoryOpt.isEmpty()) {
            return "redirect:/menu";
        }
        
        Category category = categoryOpt.get();
        List<MenuItem> menuItems = menuItemService.getMenuItemsByCategory(category);
        
        model.addAttribute("category", category);
        model.addAttribute("menuItems", menuItems);
        model.addAttribute("pageTitle", category.getName() + " - " + category.getMenuType().getName());
        
        return "menu/category-items";
    }
    
    @GetMapping("/item/{itemId}")
    public String viewMenuItem(@PathVariable Long itemId, Model model) {
        Optional<MenuItem> menuItemOpt = menuItemService.getMenuItemById(itemId);
        if (menuItemOpt.isEmpty()) {
            return "redirect:/menu";
        }
        
        MenuItem menuItem = menuItemOpt.get();
        model.addAttribute("menuItem", menuItem);
        model.addAttribute("pageTitle", menuItem.getName());
        
        return "menu/item-detail";
    }
    
    @GetMapping("/search")
    public String searchMenu(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<MenuItem> searchResults;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            searchResults = menuItemService.searchMenuItems(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            searchResults = menuItemService.getAllAvailableMenuItems();
        }
        
        model.addAttribute("menuItems", searchResults);
        model.addAttribute("pageTitle", "Search Results");
        
        return "menu/search-results";
    }
    
    @GetMapping("/popular")
    public String popularItems(Model model) {
        List<MenuItem> popularItems = menuItemService.getPopularMenuItems();
        model.addAttribute("menuItems", popularItems);
        model.addAttribute("pageTitle", "Popular Items");
        
        return "menu/popular-items";
    }
}
