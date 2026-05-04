package com.mangaone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.mangaone.entity.CartItem;
import java.util.List;

@Repository 
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    // GIẢI QUYẾT LỖI 1: Khai báo phương thức tìm kiếm theo UserId
    // JPA sẽ tự hiểu và sinh code xử lý cho bạn
	List<CartItem> findByUserUserId(Long userId);
}