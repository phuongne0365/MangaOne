package com.mangaone.controller;

import com.mangaone.service.MangaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // Đánh dấu đây là lớp điều hướng web
public class MangaController {

    @Autowired
    private MangaService mangaService; // Gọi "Bếp" MangaService ra dùng

    @GetMapping("/kho-truyen") // Khi gõ /kho-truyen trên trình duyệt thì hàm này chạy
    public String listMangas(Model model) {
        // Lấy danh sách truyện và đặt tên là "listMangas" để gửi ra giao diện
        model.addAttribute("listMangas", mangaService.getAllMangas());
        return "manga-list"; // Sẽ tìm file manga-list.html trong thư mục templates
    }
}