package com.mangaone.service;

import com.mangaone.entity.CartItem;
import com.mangaone.entity.User;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {

    /**
     * UC06 — Thêm truyện vào giỏ hàng.
     * Nghiệp vụ cốt lõi: nếu truyện đã có trong giỏ → cộng dồn số lượng,
     * KHÔNG tạo dòng mới.
     *
     * @param user     Người dùng hiện tại (lấy từ SecurityContext)
     * @param mangaId  ID cuốn truyện muốn thêm
     * @param quantity Số lượng muốn thêm (>= 1)
     */
    void addToCart(User user, Long mangaId, int quantity);

    /**
     * UC06 — Lấy toàn bộ danh sách CartItem của người dùng hiện tại
     * để hiển thị trang giỏ hàng.
     */
    List<CartItem> getCartItems(User user);

    /**
     * UC06 — Cập nhật số lượng của một CartItem cụ thể.
     * Nếu quantity <= 0, tự động xóa dòng đó khỏi giỏ.
     *
     * @param cartId   ID dòng CartItem cần cập nhật
     * @param quantity Số lượng mới
     */
    void updateQuantity(Integer cartId, int quantity);

    /**
     * UC06 — Xóa một truyện cụ thể khỏi giỏ hàng.
     *
     * @param cartId ID dòng CartItem cần xóa
     */
    void removeFromCart(Integer cartId);

    /**
     * UC07 — Xóa toàn bộ giỏ hàng sau khi đặt hàng thành công.
     */
    void clearCart(User user);

    /**
     * UC06 — Tính tổng tiền tạm tính của giỏ hàng.
     */
    Double calculateTotal(List<CartItem> cartItems);
}
