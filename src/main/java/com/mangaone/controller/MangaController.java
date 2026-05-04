package com.mangaone.controller;

import com.mangaone.entity.Manga;
import com.mangaone.service.CategoryService;
import com.mangaone.service.MangaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller // Đánh dấu đây là lớp điều hướng web
public class MangaController {

    @Autowired
    private MangaService mangaService; // Gọi "Bếp" MangaService ra dùng

    @Autowired
    private CategoryService categoryService; // Dùng để hiển thị menu thể loại trên navbar

    @GetMapping("/kho-truyen") // Khi gõ /kho-truyen trên trình duyệt thì hàm này chạy
    public String listMangas(Model model) {
        // Lấy danh sách truyện và đặt tên là "listMangas" để gửi ra giao diện
        model.addAttribute("listMangas", mangaService.getAllMangas());
        // Thêm danh sách categories để hiển thị menu trên header
        model.addAttribute("categories", categoryService.getAllCategories());
        return "manga-list"; // Sẽ tìm file manga-list.html trong thư mục templates
    }

    // ================= MỞ TRANG CHI TIẾT TRUYỆN =================
    @GetMapping("/manga/{id}")
    public String showMangaDetail(@PathVariable("id") Long id, Model model) {
        Manga manga = mangaService.getMangaById(id); // Lấy thông tin chi tiết truyện

        if (manga != null) {
            model.addAttribute("manga", manga);
            // Giữ lại menu thể loại trên thanh điều hướng
            model.addAttribute("categories", categoryService.getAllCategories());
            return "manga-detail"; // Trỏ tới file manga-detail.html
        }

        return "redirect:/kho-truyen"; // Nếu không tìm thấy truyện, quay lại kho truyện
    }
}