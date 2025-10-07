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

import com.partymenu.webapp.entity.MenuType;
import com.partymenu.webapp.service.MenuTypeService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/menu-types")
public class AdminMenuTypeController {

    private final MenuTypeService menuTypeService;

    @Autowired
    public AdminMenuTypeController(MenuTypeService menuTypeService) {
        this.menuTypeService = menuTypeService;
    }

    /**
     * Display list of all menu types
     */
    @GetMapping
    public String listMenuTypes(Model model) {
        List<MenuType> menuTypes = menuTypeService.getAllMenuTypes();
        model.addAttribute("menuTypes", menuTypes);
        model.addAttribute("pageTitle", "Manage Menu Types");
        return "admin/menu-types/list";
    }

    /**
     * Show form to create new menu type
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        MenuType menuType = new MenuType();
        menuType.setIsActive(true); // Default to active

        model.addAttribute("menuType", menuType);
        model.addAttribute("pageTitle", "Add New Menu Type");
        return "admin/menu-types/form";
    }

    /**
     * Save menu type (both create and update)
     */
    @PostMapping("/save")
    public String saveMenuType(@Valid @ModelAttribute MenuType menuType, 
                             BindingResult result, 
                             Model model, 
                             RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("pageTitle", 
                menuType.getId() == null ? "Add New Menu Type" : "Edit Menu Type");
            return "admin/menu-types/form";
        }

        try {
            MenuType savedMenuType = menuTypeService.saveMenuType(menuType);
            String action = menuType.getId() == null ? "created" : "updated";
            redirectAttributes.addFlashAttribute("successMessage", 
                "Menu Type '" + savedMenuType.getName() + "' has been " + action + " successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error saving menu type: " + e.getMessage());
        }

        return "redirect:/admin/menu-types";
    }

    /**
     * Show form to edit existing menu type
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, 
                             Model model, 
                             RedirectAttributes redirectAttributes) {

        Optional<MenuType> menuTypeOpt = menuTypeService.getMenuTypeById(id);
        if (menuTypeOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Menu Type not found with ID: " + id);
            return "redirect:/admin/menu-types";
        }

        model.addAttribute("menuType", menuTypeOpt.get());
        model.addAttribute("pageTitle", "Edit Menu Type");
        return "admin/menu-types/form";
    }

    /**
     * Delete menu type
     */
    @GetMapping("/delete/{id}")
    public String deleteMenuType(@PathVariable Long id, 
                               RedirectAttributes redirectAttributes) {

        try {
            Optional<MenuType> menuTypeOpt = menuTypeService.getMenuTypeById(id);
            if (menuTypeOpt.isPresent()) {
                MenuType menuType = menuTypeOpt.get();

                // Check if menu type has categories
                if (menuType.getCategories() != null && !menuType.getCategories().isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "Cannot delete menu type '" + menuType.getName() + 
                        "' because it has associated categories. Please delete the categories first.");
                } else {
                    menuTypeService.deleteMenuType(id);
                    redirectAttributes.addFlashAttribute("successMessage", 
                        "Menu Type '" + menuType.getName() + "' has been deleted successfully!");
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Menu Type not found with ID: " + id);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error deleting menu type: " + e.getMessage());
        }

        return "redirect:/admin/menu-types";
    }

    /**
     * Toggle active status of menu type
     */
    @GetMapping("/toggle-active/{id}")
    public String toggleActiveStatus(@PathVariable Long id, 
                                   RedirectAttributes redirectAttributes) {

        try {
            Optional<MenuType> menuTypeOpt = menuTypeService.getMenuTypeById(id);
            if (menuTypeOpt.isPresent()) {
                MenuType menuType = menuTypeOpt.get();
                menuType.setIsActive(!menuType.getIsActive());
                MenuType updatedMenuType = menuTypeService.saveMenuType(menuType);

                String status = updatedMenuType.getIsActive() ? "activated" : "deactivated";
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Menu Type '" + updatedMenuType.getName() + "' has been " + status + " successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Menu Type not found with ID: " + id);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error updating menu type status: " + e.getMessage());
        }

        return "redirect:/admin/menu-types";
    }

    /**
     * View menu type details
     */
    @GetMapping("/view/{id}")
    public String viewMenuType(@PathVariable Long id, 
                             Model model, 
                             RedirectAttributes redirectAttributes) {

        Optional<MenuType> menuTypeOpt = menuTypeService.getMenuTypeById(id);
        if (menuTypeOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Menu Type not found with ID: " + id);
            return "redirect:/admin/menu-types";
        }

        MenuType menuType = menuTypeOpt.get();
        model.addAttribute("menuType", menuType);
        model.addAttribute("pageTitle", "View Menu Type: " + menuType.getName());

        // Add categories count
        int categoriesCount = menuType.getCategories() != null ? menuType.getCategories().size() : 0;
        model.addAttribute("categoriesCount", categoriesCount);

        return "admin/menu-types/view";
    }
}