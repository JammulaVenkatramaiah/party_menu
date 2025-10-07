package com.partymenu.webapp.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.partymenu.webapp.entity.Category;
import com.partymenu.webapp.entity.MenuType;
import com.partymenu.webapp.service.CategoryService;
import com.partymenu.webapp.service.MenuTypeService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final MenuTypeService menuTypeService;

    @Autowired
    public AdminCategoryController(CategoryService categoryService, MenuTypeService menuTypeService) {
        this.categoryService = categoryService;
        this.menuTypeService = menuTypeService;
    }

    @GetMapping
    public String listCategories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("pageTitle", "Manage Categories");
        return "admin/categories/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Category category = new Category();
        category.setIsActive(true);
        category.setDisplayOrder(1);

        List<MenuType> menuTypes = menuTypeService.getAllActiveMenuTypes();
        model.addAttribute("category", category);
        model.addAttribute("menuTypes", menuTypes);
        model.addAttribute("pageTitle", "Add New Category");
        return "admin/categories/form";
    }

    @PostMapping("/save")
    public String saveCategory(@Valid @ModelAttribute Category category, BindingResult result, 
                             Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            List<MenuType> menuTypes = menuTypeService.getAllActiveMenuTypes();
            model.addAttribute("menuTypes", menuTypes);
            model.addAttribute("pageTitle", category.getId() == null ? "Add New Category" : "Edit Category");
            return "admin/categories/form";
        }

        try {
            categoryService.saveCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Category '" + category.getName() + "' has been saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error saving category: " + e.getMessage());
        }

        return "redirect:/admin/categories";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Category> categoryOpt = categoryService.getCategoryById(id);
        if (categoryOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Category not found!");
            return "redirect:/admin/categories";
        }

        List<MenuType> menuTypes = menuTypeService.getAllActiveMenuTypes();
        model.addAttribute("category", categoryOpt.get());
        model.addAttribute("menuTypes", menuTypes);
        model.addAttribute("pageTitle", "Edit Category");
        return "admin/categories/form";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Category> categoryOpt = categoryService.getCategoryById(id);
            if (categoryOpt.isPresent()) {
                categoryService.deleteCategory(id);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Category '" + categoryOpt.get().getName() + "' has been deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Category not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error deleting category: " + e.getMessage());
        }

        return "redirect:/admin/categories";
    }

    @GetMapping("/toggle-active/{id}")
    public String toggleActiveStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.toggleActiveStatus(id);
            String status = category.getIsActive() ? "activated" : "deactivated";
            redirectAttributes.addFlashAttribute("successMessage", 
                "Category '" + category.getName() + "' has been " + status + " successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error updating category status: " + e.getMessage());
        }

        return "redirect:/admin/categories";
    }
}