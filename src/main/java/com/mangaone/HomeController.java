package com.mangaone;

import com.mangaone.repository.MangaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private MangaRepository mangaRepository; // Kết nối với kho dữ liệu

    @GetMapping("/")
    public String index(Model model) {
        // Lấy danh sách truyện và đặt tên là "listManga" để dùng bên HTML
        model.addAttribute("listManga", mangaRepository.findAll());
        return "index";
    }
}