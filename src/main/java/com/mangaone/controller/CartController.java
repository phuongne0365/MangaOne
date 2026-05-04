package com.mangaone.controller;

import org.springframework.web.bind.annotation.*;
import com.mangaone.entity.CartItem;
import com.mangaone.service.CartService;
import java.util.List;

@RestController // Trả về dữ liệu dạng JSON cho Frontend
@CrossOrigin(origins = "http://localhost:5174")// Cho phép ReactJS truy cập (Mở khóa CORS)[cite: 1]
@RequestMapping("/api/cart") // Đường dẫn gốc là /api/cart
public class CartController {

    private final CartService cartService;

    // Tiêm (Inject) Service vào Controller thông qua Constructor[cite: 1]
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // API: Lấy giỏ hàng theo User ID (Ví dụ: GET /api/cart/1)[cite: 1]
    @GetMapping("/{userId}")
    public List<CartItem> getCart(@PathVariable Long userId) {
        return cartService.getCartByUser(userId);
    }

    // API: Thêm sản phẩm vào giỏ (Ví dụ: POST /api/cart)[cite: 1]
    @PostMapping
    public CartItem add(@RequestBody CartItem item) {
        return cartService.saveToCart(item);
    }

    // API: Xóa sản phẩm khỏi giỏ (Ví dụ: DELETE /api/cart/5)[cite: 1]
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        cartService.deleteFromCart(id);
        return "Xóa thành công!";
    }
}