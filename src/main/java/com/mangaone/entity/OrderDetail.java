package com.mangaone.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ORDER_DETAILS")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private Long orderDetailId;

    // Nhiều OrderDetail thuộc về 1 Order
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    // Nhiều OrderDetail chứa 1 Manga
    @ManyToOne
    @JoinColumn(name = "manga_id")
    private Manga manga;

    private Integer quantity;

    private Integer price;

    // Getter Setter

    public Long getOrderDetailId() {
        return orderDetailId;
    }

    public void setOrderDetailId(Long orderDetailId) {
        this.orderDetailId = orderDetailId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Manga getManga() {
        return manga;
    }

    public void setManga(Manga manga) {
        this.manga = manga;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}