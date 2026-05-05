package com.mangaone.service;

import com.mangaone.entity.CartItem;
import com.mangaone.entity.User;

import java.util.List;

/**
 * Interface định nghĩa các hành vi (nghiệp vụ) của Module Giỏ hàng.
 * Controller chỉ gọi các phương thức này — không cần biết cách cài đặt bên trong.
 * Đây là nguyên tắc "Dependency Inversion" trong mô hình MVC của Spring Boot.
 */
public interface CartService {

    /** Thêm truyện vào giỏ (tự động cộng dồn nếu đã có) */
    void addToCart(User user, Long mangaId, int quantity);

    /** Lấy toàn bộ danh sách giỏ hàng của user */
    List<CartItem> getCartItems(User user);

    /** Cập nhật số lượng (nếu quantity <= 0 thì tự xóa dòng đó) */
    void updateQuantity(Integer cartId, int quantity);

    /** Xóa 1 truyện khỏi giỏ theo cartId */
    void removeFromCart(Integer cartId);

    /** Xóa sạch giỏ hàng sau khi đặt hàng */
    void clearCart(User user);

    /** Tính tổng tiền: Σ (price × quantity) */
    Double calculateTotal(List<CartItem> cartItems);
}