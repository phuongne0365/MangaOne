package com.mangaone.repository;

import com.mangaone.entity.CartItem;
import com.mangaone.entity.User;
import com.mangaone.entity.Manga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    /**
     * Lấy toàn bộ giỏ hàng của một người dùng cụ thể.
     * Dùng trong CartService để hiển thị giỏ hàng (UC06).
     */
    List<CartItem> findByUser(User user);

    /**
     * Tìm một dòng CartItem cụ thể theo User VÀ Manga.
     * Dùng để kiểm tra "đã có trong giỏ chưa?" — nghiệp vụ cộng dồn số lượng (UC06).
     */
    Optional<CartItem> findByUserAndManga(User user, Manga manga);

    /**
     * Xóa toàn bộ giỏ hàng của user sau khi đặt hàng thành công (UC07).
     */
    void deleteByUser(User user);
}
