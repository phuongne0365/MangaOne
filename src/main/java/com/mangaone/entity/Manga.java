package com.mangaone.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "MANGAS") // Viết hoa cho chuẩn với bảng SQL của bạn
public class Manga {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manga_id") // Ánh xạ đúng vào cột manga_id
    private Long id;

    private String title;       
    private String author;      
    private Double price;       
    
    @Column(name = "image_url") // Ánh xạ đúng vào cột image_url
    private String image;       
    
    @Column(columnDefinition = "TEXT")
    private String description; 

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