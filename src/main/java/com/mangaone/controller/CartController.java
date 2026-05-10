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

        // Chưa đăng nhập → chuyển về trang login
        if (user == null) {
            return "redirect:/login";
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
            return "redirect:/login";
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
                                 HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        // Service tự xử lý: nếu quantity <= 0 thì xóa luôn
        cartService.updateQuantity(cartId, quantity);

        return "redirect:/cart";  
    }

    // XÓA KHỎI GIỎ: POST /cart/remove/{cartId}
    @PostMapping("/remove/{cartId}")
    public String xoaKhoiGio(@PathVariable Integer cartId,
                              HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        cartService.removeFromCart(cartId);

        return "redirect:/cart";
    }
}