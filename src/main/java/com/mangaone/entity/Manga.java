package com.mangaone.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "mangas") // Tên bảng sẽ xuất hiện trong MySQL
public class Manga {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID tự tăng
    private Long id;

    private String title;       // Tên truyện
    private String author;      // Tác giả
    private Double price;       // Giá tiền
    private String image;       // Tên file ảnh (vd: book1.jpg)
    
    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả truyện

    // --- Bắt đầu phần Getter và Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}