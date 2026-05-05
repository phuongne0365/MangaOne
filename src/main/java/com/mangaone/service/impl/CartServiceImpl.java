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
 * Lớp cài đặt (Implementation) của CartService.
 * Chứa toàn bộ logic nghiệp vụ của Module Giỏ hàng.
 *
 * @Service  → Spring nhận diện đây là lớp Service, tự tạo Bean và quản lý vòng đời.
 * @Transactional → Đảm bảo mỗi phương thức là 1 giao dịch DB (rollback nếu có lỗi).
 */
@Service
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final MangaRepository mangaRepository;

    // Inject dependency qua Constructor (cách được Spring khuyến nghị)
    public CartServiceImpl(CartItemRepository cartItemRepository,
                           MangaRepository mangaRepository) {
        this.cartItemRepository = cartItemRepository;
        this.mangaRepository = mangaRepository;
    }

    /**
     * THÊM TRUYỆN VÀO GIỎ HÀNG
     *
     * Luồng xử lý:
     *   1. Kiểm tra số lượng hợp lệ (> 0)
     *   2. Tìm Manga theo ID → ném lỗi nếu không tồn tại
     *   3. Tìm xem user đã có manga này trong giỏ chưa
     *      - Có rồi  → cộng dồn số lượng, lưu lại
     *      - Chưa có → tạo CartItem mới, lưu vào DB
     */
    @Override
    @Transactional
    public void addToCart(User user, Long mangaId, int quantity) {
        // Bước 1: Validate đầu vào
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
        }

        // Bước 2: Lấy thông tin Manga từ DB
        Manga manga = mangaRepository.findById(mangaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy truyện với ID: " + mangaId));

        // Bước 3: Kiểm tra tồn kho
        if (manga.getStockQuantity() <= 0) {
            throw new IllegalStateException(
                    "Truyện \"" + manga.getTitle() + "\" hiện đã hết hàng.");
        }

        // Bước 4: Tìm xem đã có trong giỏ chưa
        Optional<CartItem> existing = cartItemRepository.findByUserAndManga(user, manga);

        if (existing.isPresent()) {
            // ĐÃ CÓ → Cộng dồn số lượng
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            // CHƯA CÓ → Tạo dòng mới
            CartItem newItem = new CartItem();
            newItem.setUser(user);
            newItem.setManga(manga);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
        }
    }

    /**
     * LẤY DANH SÁCH GIỎ HÀNG
     * readOnly = true → Spring tối ưu: không khóa bảng, chỉ đọc.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(User user) {
        return cartItemRepository.findByUser(user);
    }

    /**
     * CẬP NHẬT SỐ LƯỢNG
     *
     * Luồng xử lý:
     *   - Tìm CartItem theo cartId → ném lỗi nếu không tìm thấy
     *   - quantity <= 0 → xóa dòng đó khỏi giỏ
     *   - quantity > 0  → cập nhật số lượng mới
     */
    @Override
    @Transactional
    public void updateQuantity(Integer cartId, int quantity) {
        CartItem item = cartItemRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy CartItem với ID: " + cartId));

        if (quantity <= 0) {
            // Số lượng = 0 → coi như người dùng muốn xóa
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
     * XÓA SẠCH GIỎ HÀNG (gọi sau khi đặt hàng thành công)
     */
    @Override
    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }

    /**
     * TÍNH TỔNG TIỀN
     * Công thức: Σ (price × quantity) của từng dòng trong giỏ.
     * Dùng Stream API để duyệt danh sách và tính tổng.
     */
    @Override
    public Double calculateTotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .mapToDouble(item -> item.getManga().getPrice() * item.getQuantity())
                .sum();
    }
}