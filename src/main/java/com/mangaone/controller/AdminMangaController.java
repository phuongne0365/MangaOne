package com.mangaone.controller;

import com.mangaone.entity.Category;
import com.mangaone.entity.Manga;
import com.mangaone.entity.Publisher;
import com.mangaone.repository.CategoryRepository;
import com.mangaone.repository.MangaRepository;
import com.mangaone.repository.PublisherRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AdminMangaController {

    @Autowired
    private MangaRepository mangaRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    // helper: lấy danh sách mangas sắp xếp theo id giảm dần (mới nhất trước)
    private List<Manga> findAllMangasSorted() {
        return mangaRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    // LIST
    @GetMapping("/admin/mangas")
    public String list(Model model) {
        model.addAttribute("currentPage", "mangas");
        model.addAttribute("mangas", findAllMangasSorted());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("publishers", publisherRepository.findAll());
        model.addAttribute("manga", new Manga());
        return "admin/manga-list";
    }

    // SAVE (ADD + UPDATE)
    @PostMapping("/admin/mangas/save")
    public String save(
            @ModelAttribute Manga manga,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "publisherId", required = false) Integer publisherId,
            @RequestParam(value = "imageUrl", required = false) String imageUrl) {

        // VALIDATION
        if (manga.getTitle() == null || manga.getTitle().trim().isEmpty()) {
            return "redirect:/admin/mangas";
        }
        if (manga.getAuthor() == null || manga.getAuthor().trim().isEmpty()) {
            return "redirect:/admin/mangas";
        }
        if (manga.getPrice() == null || manga.getPrice() < 0) {
            return "redirect:/admin/mangas";
        }
        if (manga.getStockQuantity() == null || manga.getStockQuantity() < 0) {
            return "redirect:/admin/mangas";
        }

        // CATEGORY
        if (categoryId != null) {
            Category category = new Category();
            category.setCategoryId(categoryId);
            manga.setCategory(category);
        }

        // PUBLISHER
        if (publisherId != null) {
            Publisher publisher = new Publisher();
            publisher.setPublisherId(publisherId);
            manga.setPublisher(publisher);
        }

        // ẢNH: Dùng URL được nhập từ form
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            // Admin nhập URL mới → lưu URL đó
            manga.setImage(imageUrl.trim());
        } else {
            // Không nhập URL → giữ nguyên ảnh cũ khi edit
            if (manga.getId() != null) {
                Manga oldManga = mangaRepository.findById(manga.getId()).orElse(null);
                if (oldManga != null) {
                    manga.setImage(oldManga.getImage());
                }
            }
        }

        // SAVE
        mangaRepository.save(manga);
        return "redirect:/admin/mangas";
    }

    // EDIT
    @GetMapping("/admin/mangas/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Manga manga = mangaRepository.findById(id).orElse(null);
        model.addAttribute("manga", manga);
        model.addAttribute("mangas", findAllMangasSorted());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("publishers", publisherRepository.findAll());
        return "admin/manga-list";
    }

    // DELETE
    @GetMapping("/admin/mangas/delete/{id}")
    public String delete(@PathVariable Long id) {
        mangaRepository.deleteById(id);
        return "redirect:/admin/mangas";
    }
}