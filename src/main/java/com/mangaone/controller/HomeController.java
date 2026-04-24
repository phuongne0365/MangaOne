package com.mangaone.controller;

import com.mangaone.entity.CartItem;
import com.mangaone.entity.User;
import com.mangaone.service.CartService;
import com.mangaone.service.CategoryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Ví dụ cách tích hợp CategoryService & CartService vào Controller.
 * Copy và điều chỉnh vào HomeController.java thực tế của dự án.
 */
@Controller
public class HomeController {

    private final CategoryService categoryService;
    private final CartService cartService;

    public HomeController(CategoryService categoryService, CartService cartService) {
        this.categoryService = categoryService;
        this.cartService = cartService;
    }

    /**
     * UC01 + UC02 — Trang chủ: đẩy danh sách Category vào Model
     * để Thymeleaf render menu điều hướng (navbar).
     *
     * Trong template: th:each="cat : ${categories}"
     */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        // Thêm các attribute khác (mangas, v.v.) ở đây
        return "index";
    }

    // ----------------------------------------------------------------
    //  GIỎ HÀNG — UC06
    // ----------------------------------------------------------------

    /** Hiển thị trang giỏ hàng */
    @GetMapping("/cart")
    public String viewCart(@AuthenticationPrincipal User currentUser, Model model) {
        List<CartItem> items = cartService.getCartItems(currentUser);
        BigDecimal total = cartService.calculateTotal(items);

        model.addAttribute("cartItems", items);
        model.addAttribute("total", total);
        model.addAttribute("categories", categoryService.getAllCategories()); // cho navbar
        return "cart";
    }

    /** Thêm truyện vào giỏ — gọi từ trang chi tiết truyện */
    @PostMapping("/cart/add")
    public String addToCart(@AuthenticationPrincipal User currentUser,
                            @RequestParam Long mangaId,
                            @RequestParam(defaultValue = "1") int quantity) {
        cartService.addToCart(currentUser, mangaId, quantity);
        return "redirect:/cart";
    }

    /** Cập nhật số lượng — nút +/- trong trang giỏ hàng */
    @PostMapping("/cart/update")
    public String updateQuantity(@RequestParam Integer cartId,
                                 @RequestParam int quantity) {
        cartService.updateQuantity(cartId, quantity);
        return "redirect:/cart";
    }

    /** Xóa một sản phẩm khỏi giỏ */
    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Integer cartId) {
        cartService.removeFromCart(cartId);
        return "redirect:/cart";
    }
}
