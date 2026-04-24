package com.mangaone.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "CART_ITEMS")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Integer cartId;

    // Quan hệ ManyToOne đến User (user_id INT theo SQL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Quan hệ ManyToOne đến Manga (manga_id BIGINT theo SQL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manga_id", nullable = false)
    private Manga manga;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    public Integer getCartId() { return cartId; }
    public void setCartId(Integer cartId) { this.cartId = cartId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Manga getManga() { return manga; }
    public void setManga(Manga manga) { this.manga = manga; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
