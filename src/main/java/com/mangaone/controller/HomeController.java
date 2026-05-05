package com.mangaone.controller;

import com.mangaone.entity.CartItem;
import com.mangaone.entity.User;
import com.mangaone.repository.MangaRepository; // Mang từ file cũ sang
import com.mangaone.service.CartService;
import com.mangaone.service.CategoryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class HomeController {

    private final CategoryService categoryService;
    private final CartService cartService;
    private final MangaRepository mangaRepository; // Thêm kho truyện vào đây

    // Kết nối các Service và Repository
    public HomeController(CategoryService categoryService, CartService cartService, MangaRepository mangaRepository) {
        this.categoryService = categoryService;
        this.cartService = cartService;
        this.mangaRepository = mangaRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        // 1. Lấy danh sách Thể loại để làm Menu Navbar (Của AI)
        model.addAttribute("categories", categoryService.getAllCategories());
        
        // 2. Lấy danh sách Truyện để hiển thị ra giữa màn hình (Của Sơn)
        model.addAttribute("listManga", mangaRepository.findAll());
        
        return "index";
    }

    // ----------------------------------------------------------------
    //  GIỎ HÀNG — UC06 (Giữ nguyên của AI)
    // ----------------------------------------------------------------

//    @GetMapping("/cart")
//    public String viewCart(@AuthenticationPrincipal User currentUser, Model model) {
//        List<CartItem> items = cartService.getCartItems(currentUser);
//        Double total = cartService.calculateTotal(items);
//
//        model.addAttribute("cartItems", items);
//        model.addAttribute("total", total);
//        model.addAttribute("categories", categoryService.getAllCategories()); 
//        return "cart";
//    }
//
//    @PostMapping("/cart/add")
//    public String addToCart(@AuthenticationPrincipal User currentUser,
//                            @RequestParam Long mangaId,
//                            @RequestParam(defaultValue = "1") int quantity) {
//        cartService.addToCart(currentUser, mangaId, quantity);
//        return "redirect:/cart";
//    }
//
//    @PostMapping("/cart/update")
//    public String updateQuantity(@RequestParam Integer cartId,
//                                 @RequestParam int quantity) {
//        cartService.updateQuantity(cartId, quantity);
//        return "redirect:/cart";
//    }
//
//    @PostMapping("/cart/remove")
//    public String removeFromCart(@RequestParam Integer cartId) {
//        cartService.removeFromCart(cartId);
//        return "redirect:/cart";
//    }
}