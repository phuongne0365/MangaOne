package com.mangaone.controller;

import com.mangaone.entity.CartItem;
import com.mangaone.entity.User;
import com.mangaone.repository.MangaRepository;
import com.mangaone.service.CartService;
import com.mangaone.service.CategoryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class HomeController {

    private final CategoryService categoryService;
    private final CartService cartService;
    private final MangaRepository mangaRepository; 

    // Constructor injection - Kết nối các Service và Repository
    public HomeController(CategoryService categoryService, CartService cartService, MangaRepository mangaRepository) {
        this.categoryService = categoryService;
        this.cartService = cartService;
        this.mangaRepository = mangaRepository;
    }

    // 1. TRANG CHỦ
    @GetMapping("/")
    public String home(Model model, HttpSession session,
                       @RequestParam(value = "openLogin", required = false) String openLogin,
                       @RequestParam(value = "openRegister", required = false) String openRegister) {
        
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("listManga", mangaRepository.findAll());
        model.addAttribute("bestSellers", mangaRepository.findTopBestSellers());

        // Xử lý thông báo từ Session (Login/Register)
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

        // Tự động mở modal nếu có yêu cầu
        if (openLogin != null) model.addAttribute("openLogin", true);
        if (openRegister != null) model.addAttribute("openRegister", true);
        
        return "index";
    }

    // ----------------------------------------------------------------
    // 2. GIỎ HÀNG (Đã mở khóa và tối ưu)
    // ----------------------------------------------------------------

    @GetMapping("/cart")
    public String viewCart(@AuthenticationPrincipal User currentUser, Model model) {
        if (currentUser == null) return "redirect:/?openLogin=true"; // Bảo vệ trang giỏ hàng
        
        List<CartItem> items = cartService.getCartItems(currentUser);
        Double total = cartService.calculateTotal(items);

        model.addAttribute("cartItems", items);
        model.addAttribute("total", total);
        model.addAttribute("categories", categoryService.getAllCategories()); 
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@AuthenticationPrincipal User currentUser,
                            @RequestParam Long mangaId,
                            @RequestParam(defaultValue = "1") int quantity) {
        if (currentUser == null) return "redirect:/?openLogin=true";
        
        cartService.addToCart(currentUser, mangaId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    public String updateQuantity(@RequestParam Integer cartId,
                                 @RequestParam int quantity) {
        cartService.updateQuantity(cartId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Integer cartId) {
        cartService.removeFromCart(cartId);
        return "redirect:/cart";
    }

    // ----------------------------------------------------------------
    // 3. CÁC TRANG PHỤ
    // ----------------------------------------------------------------

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "about";
    }

    @GetMapping("/news")
    public String news(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "news";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "contact";
    }
}