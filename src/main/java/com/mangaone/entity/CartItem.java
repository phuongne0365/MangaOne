package com.mangaone.entity;

import jakarta.persistence.*;

/**
 * Entity ánh xạ bảng CART_ITEMS trong database.
 * Mỗi CartItem = 1 dòng trong giỏ hàng (1 User + 1 Manga + số lượng).
 */
@Entity
@Table(name = "CART_ITEMS")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Integer cartId;

    /**
     * Quan hệ N-1 với User: nhiều CartItem thuộc về 1 User.
     * FetchType.LAZY = chỉ tải User khi thực sự cần (tối ưu hiệu năng).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Quan hệ N-1 với Manga: nhiều CartItem có thể trỏ đến 1 Manga.
     * FetchType.EAGER = tải luôn Manga khi load CartItem (cần để hiển thị tên, giá).
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manga_id", nullable = false)
    private Manga manga;

    /** Số lượng cuốn truyện trong giỏ */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // ===================== Getter & Setter =====================

    public Integer getCartId() { return cartId; }
    public void setCartId(Integer cartId) { this.cartId = cartId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Manga getManga() { return manga; }
    public void setManga(Manga manga) { this.manga = manga; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    /**
     * Tính thành tiền của 1 dòng giỏ hàng: Đơn giá × Số lượng.
     * Gọi trực tiếp từ Thymeleaf: ${item.subtotal}
     */
    public double getSubtotal() {
        if (manga == null || manga.getPrice() == null) return 0;
        return manga.getPrice() * quantity;
    }
}