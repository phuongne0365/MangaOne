package com.mangaone.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mangaone.repository.MangaRepository;
import com.mangaone.service.CartService;
import com.mangaone.service.CategoryService;

import jakarta.servlet.http.HttpSession;

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
                       @RequestParam(value = "openLogin",    required = false) String openLogin,
                       @RequestParam(value = "openRegister", required = false) String openRegister,
                       @RequestParam(value = "loginError",   required = false) String loginErrorParam) {

        model.addAttribute("categories",  categoryService.getAllCategories());
        model.addAttribute("listManga",   mangaRepository.findAll());
        model.addAttribute("bestSellers", mangaRepository.findTop5ByOrderByStockQuantityAsc());

        // Lỗi đăng nhập: ưu tiên từ request param (Spring Security failureUrl)
        // sau đó kiểm tra session (fallback cũ)
        if (loginErrorParam != null && !loginErrorParam.isBlank()) {
            model.addAttribute("loginError", loginErrorParam);
            model.addAttribute("openLogin", true);
        } else if (session.getAttribute("loginError") != null) {
            model.addAttribute("loginError", session.getAttribute("loginError"));
            session.removeAttribute("loginError");
            model.addAttribute("openLogin", true);
        }

        // Thông báo đăng ký lỗi / thành công
        if (session.getAttribute("registerError") != null) {
            model.addAttribute("registerError", session.getAttribute("registerError"));
            session.removeAttribute("registerError");
        }
        if (session.getAttribute("registerSuccess") != null) {
            model.addAttribute("registerSuccess", session.getAttribute("registerSuccess"));
            session.removeAttribute("registerSuccess");
        }

        // Tự động mở modal nếu có yêu cầu
        if (openLogin    != null) model.addAttribute("openLogin",    true);
        if (openRegister != null) model.addAttribute("openRegister", true);

        return "index";
    }

    // ----------------------------------------------------------------
    // 2. GIỎ HÀNG (Đã mở khóa và tối ưu)
    // ----------------------------------------------------------------

  /* 

    @PostMapping("/cart/add")
    public String addToCart(@AuthenticationPrincipal User currentUser,
                            @RequestParam Long mangaId,
                            @RequestParam(defaultValue = "1") int quantity) {
        if (currentUser == null) return "redirect:/?openLogin=true";
        
        cartService.addToCart(currentUser, mangaId, quantity);
        return "redirect:/cart";
    }

   // @PostMapping("/cart/update")
   // public String updateQuantity(@RequestParam Integer cartId,
      //                           @RequestParam int quantity) {
      //  cartService.updateQuantity(cartId, quantity);
      //  return "redirect:/cart";
    //}

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Integer cartId) {
        cartService.removeFromCart(cartId);
        return "redirect:/cart";
    }
*/
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