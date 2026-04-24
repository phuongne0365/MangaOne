package com.mangaone.service.impl;

import com.mangaone.entity.CartItem;
import com.mangaone.entity.Manga;
import com.mangaone.entity.User;
import com.mangaone.repository.CartItemRepository;
import com.mangaone.repository.MangaRepository;
import com.mangaone.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final MangaRepository mangaRepository;

    public CartServiceImpl(CartItemRepository cartItemRepository,
                           MangaRepository mangaRepository) {
        this.cartItemRepository = cartItemRepository;
        this.mangaRepository = mangaRepository;
    }

    /**
     * UC06 — Thêm truyện vào giỏ hàng.
     *
     * Nghiệp vụ cốt lõi từ SRS:
     *   "Nếu khách thêm một cuốn truyện đã có sẵn trong giỏ,
     *    hệ thống sẽ TỰ ĐỘNG CỘNG DỒN SỐ LƯỢNG thay vì tạo một dòng mới."
     */
    @Override
    @Transactional
    public void addToCart(User user, Long mangaId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
        }

        // Lấy thông tin truyện, ném lỗi nếu không tồn tại
        Manga manga = mangaRepository.findById(mangaId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy truyện với ID: " + mangaId));

        // Kiểm tra tồn kho trước khi thêm (UC03)
        if (manga.getStockQuantity() <= 0) {
            throw new IllegalStateException("Truyện \"" + manga.getTitle() + "\" hiện đã hết hàng.");
        }

        // Tìm xem truyện này đã có trong giỏ của user chưa
        Optional<CartItem> existingItem = cartItemRepository.findByUserAndManga(user, manga);

        if (existingItem.isPresent()) {
            // --- Đã có trong giỏ → CỘNG DỒN số lượng ---
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            // --- Chưa có → Tạo dòng mới ---
            CartItem newItem = new CartItem();
            newItem.setUser(user);
            newItem.setManga(manga);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
        }
    }

    /**
     * UC06 — Lấy danh sách giỏ hàng để hiển thị.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(User user) {
        return cartItemRepository.findByUser(user);
    }

    /**
     * UC06 — Cập nhật số lượng (nút +/-).
     * Nếu quantity <= 0 → tự động xóa dòng đó.
     */
    @Override
    @Transactional
    public void updateQuantity(Integer cartId, int quantity) {
        CartItem item = cartItemRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy CartItem với ID: " + cartId));

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
    }

    /**
     * UC06 — Xóa một truyện khỏi giỏ.
     */
    @Override
    @Transactional
    public void removeFromCart(Integer cartId) {
        if (!cartItemRepository.existsById(cartId)) {
            throw new IllegalArgumentException("Không tìm thấy CartItem với ID: " + cartId);
        }
        cartItemRepository.deleteById(cartId);
    }

    /**
     * UC07 — Xóa toàn bộ giỏ hàng sau khi đặt hàng thành công.
     */
    @Override
    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }

    /**
     * UC06 — Tính tổng tiền tạm tính.
     * Công thức: Σ (price × quantity) của từng CartItem.
     */
    @Override
    public BigDecimal calculateTotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> item.getManga().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
