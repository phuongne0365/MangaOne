package com.mangaone.controller;

import com.mangaone.entity.CartItem;
import com.mangaone.entity.User;
import com.mangaone.repository.MangaRepository; // Mang từ file cũ sang
import com.mangaone.service.CartService;
import com.mangaone.service.CategoryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class HomeController {

    private final CategoryService categoryService;
    private final CartService cartService;
    private final MangaRepository mangaRepository; 

    // Kết nối các Service và Repository
    public HomeController(CategoryService categoryService, CartService cartService, MangaRepository mangaRepository) {
        this.categoryService = categoryService;
        this.cartService = cartService;
        this.mangaRepository = mangaRepository;
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session,
                       @RequestParam(value = "openLogin", required = false) String openLogin,
                       @RequestParam(value = "openRegister", required = false) String openRegister) {
        // 1. Lấy danh sách Thể loại để làm Menu Navbar
        model.addAttribute("categories", categoryService.getAllCategories());
        
        // 2. Lấy danh sách Truyện để hiển thị
        model.addAttribute("listManga", mangaRepository.findAll());

        // 3. Lấy thông báo lỗi/thành công từ Session rồi xóa đi 
        if (session.getAttribute("loginError") != null) {
            model.addAttribute("loginError", session.getAttribute("loginError"));
            session.removeAttribute("loginError");
        }
        if (session.getAttribute("registerError") != null) {
            model.addAttribute("registerError", session.getAttribute("registerError"));
            session.removeAttribute("registerError");
        }
        if (session.getAttribute("registerSuccess") != null) {
            model.addAttribute("registerSuccess", session.getAttribute("registerSuccess"));
            session.removeAttribute("registerSuccess");
        }

        // 4. Tự động mở modal nếu có param openLogin hoặc openRegister
        if (openLogin != null) model.addAttribute("openLogin", true);
        if (openRegister != null) model.addAttribute("openRegister", true);
        
        return "index";
    }

}