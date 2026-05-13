package com.mangaone.controller;

import com.mangaone.entity.Category;
import com.mangaone.repository.CategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminCategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    // =========================
    // LIST
    // =========================
    @GetMapping("/admin/categories")
    public String list(Model model) {

        model.addAttribute("categories", categoryRepository.findAll());

        model.addAttribute("category", new Category());

        model.addAttribute("currentPage", "categories");
        return "admin/category-list";
    }

    // =========================
    // SAVE (ADD + UPDATE)
    // =========================
    @PostMapping("/admin/categories/save")
    public String save(@ModelAttribute Category category,
                       Model model) {

        // VALIDATION
        if (category.getCategoryName() == null ||
            category.getCategoryName().trim().isEmpty()) {

            return "redirect:/admin/categories";
        }

        // CHECK DUPLICATE NAME
        for (Category c : categoryRepository.findAll()) {

            // thêm mới
            if (category.getCategoryId() == null) {

                if (c.getCategoryName().trim()
                        .equalsIgnoreCase(category.getCategoryName().trim())) {

                    model.addAttribute("error",
                            "Tên danh mục đã tồn tại!");

                    model.addAttribute("categories",
                            categoryRepository.findAll());

                    model.addAttribute("category", category);

                    return "admin/category-list";
                }
            }

            // update
            else {

                if (!c.getCategoryId().equals(category.getCategoryId())
                        &&
                    c.getCategoryName().trim()
                        .equalsIgnoreCase(category.getCategoryName().trim())) {

                    model.addAttribute("error",
                            "Tên danh mục đã tồn tại!");

                    model.addAttribute("categories",
                            categoryRepository.findAll());

                    model.addAttribute("category", category);

                    return "admin/category-list";
                }
            }
        }

        categoryRepository.save(category);

        return "redirect:/admin/categories";
    }

    // =========================
    // EDIT
    // =========================
    @GetMapping("/admin/categories/edit/{id}")
    public String edit(@PathVariable Integer id,
                       Model model) {

        Category category =
                categoryRepository.findById(id).orElse(null);

        model.addAttribute("category", category);

        model.addAttribute("categories",
                categoryRepository.findAll());

        model.addAttribute("currentPage", "categories");
        return "admin/category-list";
    }

    // =========================
    // DELETE
    // =========================
    @GetMapping("/admin/categories/delete/{id}")
    public String delete(@PathVariable Integer id,
                         Model model) {

        Category category =
                categoryRepository.findById(id).orElse(null);

        // CHECK CATEGORY HAS MANGAS
        if (category != null &&
            category.getMangas() != null &&
            !category.getMangas().isEmpty()) {

            model.addAttribute("error",
                    "Không thể xóa danh mục đang chứa truyện!");

            model.addAttribute("categories",
                    categoryRepository.findAll());

            model.addAttribute("category",
                    new Category());

            return "admin/category-list";
        }

        categoryRepository.deleteById(id);

        return "redirect:/admin/categories";
    }
}