package com.mangaone.service.impl;

import com.mangaone.entity.CartItem;
import com.mangaone.entity.Manga;
import com.mangaone.entity.User;
import com.mangaone.repository.CartItemRepository;
import com.mangaone.repository.MangaRepository;
import com.mangaone.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @Service  → Spring nhận diện đây là lớp Service, tự tạo Bean và quản lý vòng đời.
 * @Transactional → Đảm bảo mỗi phương thức là 1 giao dịch DB (rollback nếu có lỗi).
 */
@Service
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final MangaRepository mangaRepository;

    public CartServiceImpl(CartItemRepository cartItemRepository,
                           MangaRepository mangaRepository) {
        this.cartItemRepository = cartItemRepository;
        this.mangaRepository = mangaRepository;
    }

    @Override
    @Transactional
    public void addToCart(User user, Long mangaId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
        }

        Manga manga = mangaRepository.findById(mangaId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy truyện với ID: " + mangaId));

        // Tìm xem đã có trong giỏ chưa
        Optional<CartItem> existing = cartItemRepository.findByUserAndManga(user, manga);

        // BƯỚC QUAN TRỌNG: Tính tổng số lượng dự kiến sau khi thêm
        int currentInCart = existing.isPresent() ? existing.get().getQuantity() : 0;
        int totalRequested = currentInCart + quantity;

        // KIỂM TRA TỒN KHO THỰC TẾ
        if (totalRequested > manga.getStockQuantity()) {
            throw new IllegalStateException(
                    "Không thể thêm. Kho còn " + manga.getStockQuantity() + " cuốn, " +
                    "trong giỏ bạn đã có " + currentInCart + " cuốn. " +
                    "Bạn không thể mua thêm " + quantity + " cuốn nữa.");
        }

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(totalRequested); // Cập nhật tổng mới
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUser(user);
            newItem.setManga(manga);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
        }
    }
    /**
     * LẤY DANH SÁCH GIỎ HÀNG
     * readOnly = true không khóa bảng, chỉ đọc.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(User user) {
        return cartItemRepository.findByUser(user);
    }

    /**
     * CẬP NHẬT SỐ LƯỢNG
     */
    @Override
    @Transactional
    public void updateQuantity(Integer cartId, int quantity) {
        CartItem item = cartItemRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dòng này trong giỏ."));

        // KIỂM TRA TỒN KHO KHI NGƯỜI DÙNG SỬA SỐ LƯỢNG TRỰC TIẾP
        if (quantity > item.getManga().getStockQuantity()) {
            throw new IllegalStateException(
                    "Số lượng yêu cầu cho truyện \"" + item.getManga().getTitle() + 
                    "\" vượt quá tồn kho thực tế (" + item.getManga().getStockQuantity() + ").");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
    }

    /**
     * XÓA 1 TRUYỆN KHỎI GIỎ
     */
    @Override
    @Transactional
    public void removeFromCart(Integer cartId) {
        if (!cartItemRepository.existsById(cartId)) {
            throw new IllegalArgumentException(
                    "Không tìm thấy CartItem với ID: " + cartId);
        }
        cartItemRepository.deleteById(cartId);
    }

    /**
     * XÓA SẠCH GIỎ HÀNG: sau khi đặt hàng thành công
     */
    @Override
    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }

    /**
     * TÍNH TỔNG TIỀN
     * Công thức: tổng xích ma (price × quantity) của từng dòng trong giỏ.
     */
    @Override
    public Double calculateTotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .mapToDouble(item -> item.getManga().getPrice() * item.getQuantity())
                .sum();
    }
}