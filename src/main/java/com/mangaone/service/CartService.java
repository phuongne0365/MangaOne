package com.mangaone.service;

import org.springframework.stereotype.Service;
import com.mangaone.entity.CartItem;
import com.mangaone.repository.CartItemRepository;
import java.util.List;

@Service // Đánh dấu tầng xử lý nghiệp vụ
public class CartService {

    private final CartItemRepository cartRepo;

    // Tiêm (Inject) Repository thông qua constructor
    public CartService(CartItemRepository cartRepo) {
        this.cartRepo = cartRepo;
    }

    // Lấy danh sách giỏ hàng theo User ID
    public List<CartItem> getCartByUser(Long userId) {
        return cartRepo.findByUserId(userId); 
    }

    // Lưu sản phẩm vào giỏ[cite: 1]
    public CartItem saveToCart(CartItem item) {
        return cartRepo.save(item);
    }

    // Xóa sản phẩm khỏi giỏ[cite: 1]
    public void deleteFromCart(Long id) {
        cartRepo.deleteById(id);
    }
}