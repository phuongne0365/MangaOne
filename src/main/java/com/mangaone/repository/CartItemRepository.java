package com.mangaone.repository;

import com.mangaone.entity.CartItem;
import com.mangaone.entity.Manga;
import com.mangaone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho CartItem.
 * Spring Data JPA tự sinh SQL dựa trên tên phương thức — không cần viết query thủ công.
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    /**
     * Lấy toàn bộ giỏ hàng của 1 user.
     * Spring JPA dịch: SELECT * FROM CART_ITEMS WHERE user_id = ?
     */
    List<CartItem> findByUser(User user);

    /**
     * Kiểm tra xem user đã có manga này trong giỏ chưa.
     * Dùng để quyết định: thêm mới hay cộng dồn số lượng.
     * Spring JPA dịch: SELECT * FROM CART_ITEMS WHERE user_id = ? AND manga_id = ?
     */
    Optional<CartItem> findByUserAndManga(User user, Manga manga);

    /**
     * Xóa toàn bộ giỏ hàng của 1 user (gọi sau khi đặt hàng thành công).
     * Spring JPA dịch: DELETE FROM CART_ITEMS WHERE user_id = ?
     */
    void deleteByUser(User user);
}