package com.mangaone.controller;

import com.mangaone.entity.CartItem;
import com.mangaone.entity.User;
import com.mangaone.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/cart")  
public class CartController {

    private final CartService cartService;
    

    // Inject Service qua Constructor
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // HIỂN THỊ GIỎ HÀNG: GET /cart
    @GetMapping
    public String xemGioHang(HttpSession session, Model model) {
        // Lấy user đang đăng nhập từ Session
        User user = (User) session.getAttribute("loggedInUser");

        // Chưa đăng nhập → về trang chủ, mở modal đăng nhập
        if (user == null) {
            return "redirect:/?openLogin=true";
        }
        
        // Gọi Service lấy danh sách giỏ hàng
        List<CartItem> cartItems = cartService.getCartItems(user);

        // Tính tổng tiền
        Double tongTien = cartService.calculateTotal(cartItems);

        // Đưa dữ liệu vào Model để Thymeleaf đọc trong cart.html
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("tongTien", tongTien);

        return "cart";  
    }

    // THÊM VÀO GIỎ: POST /cart/add
    @PostMapping("/add")
    public String themVaoGio(@RequestParam Long mangaId,
                             @RequestParam(defaultValue = "1") int quantity,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/?openLogin=true";
        }
        
        try {
            cartService.addToCart(user, mangaId, quantity);
            redirectAttributes.addFlashAttribute("successMsg", "Đã thêm vào giỏ hàng!");
        } catch (IllegalStateException e) {
            // Lỗi hết hàng
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/cart"; 
    }

    // CẬP NHẬT SỐ LƯỢNG: POST /cart/update
    @PostMapping("/update")
    public String capNhatSoLuong(@RequestParam Integer cartId,
                                 @RequestParam int quantity,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/?openLogin=true";
        }

        try {
            // Service tự xử lý: nếu quantity <= 0 thì xóa luôn
            cartService.updateQuantity(cartId, quantity);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/cart";  
    }

    // XÓA KHỎI GIỎ: POST /cart/remove/{cartId}
    @PostMapping("/remove/{cartId}")
    public String xoaKhoiGio(@PathVariable Integer cartId,
                              HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/?openLogin=true";
        }

        cartService.removeFromCart(cartId);

        return "redirect:/cart";
    }
    @PostMapping("/update-all")
    public String capNhatTatCa(
            @RequestParam("cartIds")    List<Integer> cartIds,
            @RequestParam("quantities") List<Integer> quantities,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
     
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/?openLogin=true";
     
        // Duyệt song song 2 mảng theo index
        try {
            for (int i = 0; i < cartIds.size(); i++) {
                int qty = (i < quantities.size()) ? quantities.get(i) : 1;
                cartService.updateQuantity(cartIds.get(i), qty);
            }
            redirectAttributes.addFlashAttribute("successMsg", "✅ Đã cập nhật giỏ hàng!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        
        return "redirect:/cart";
    }
}