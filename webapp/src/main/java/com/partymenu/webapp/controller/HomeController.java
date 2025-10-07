package com.partymenu.webapp.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.partymenu.webapp.entity.MenuItem;
import com.partymenu.webapp.entity.MenuType;
import com.partymenu.webapp.service.MenuItemService;
import com.partymenu.webapp.service.MenuTypeService;

@Controller
public class HomeController {

    private final MenuTypeService menuTypeService;
    private final MenuItemService menuItemService;

    @Autowired
    public HomeController(MenuTypeService menuTypeService, MenuItemService menuItemService) {
        this.menuTypeService = menuTypeService;
        this.menuItemService = menuItemService;
    }

    @GetMapping("/")
    public String home(Model model) {
        // Get active menu types for navigation
        List<MenuType> menuTypes = menuTypeService.getAllActiveMenuTypes();
        if (menuTypes == null) {
            menuTypes = new ArrayList<>();
        }
        model.addAttribute("menuTypes", menuTypes);

        // Get popular menu items for homepage display
        List<MenuItem> popularItems = menuItemService.getRandomPopularItems(6);
        if (popularItems == null) {
            popularItems = new ArrayList<>();
        }
        model.addAttribute("popularItems", popularItems);

        // Get some statistics
        long menuTypeCount = menuTypeService.getActiveMenuTypeCount();
        model.addAttribute("menuTypeCount", menuTypeCount);
        model.addAttribute("pageTitle", "Welcome to Party Menu");

        return "home";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "About Us - Party Menu");
        return "about";
    }

    @GetMapping("/privacy")
    public String privacy(Model model) {
        model.addAttribute("pageTitle", "Privacy Policy - Party Menu");
        return "privacy-policy";
    }

    @GetMapping("/terms")
    public String terms(Model model) {
        model.addAttribute("pageTitle", "Terms of Service - Party Menu");
        return "terms-of-service";
    }

    @GetMapping("/faq")
    public String faq(Model model) {
        model.addAttribute("pageTitle", "FAQ - Party Menu");
        return "faq";
    }
}