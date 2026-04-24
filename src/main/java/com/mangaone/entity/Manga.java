package com.mangaone.entity;

import jakarta.persistence.*;
import com.mangaone.entity.Category;
import com.mangaone.entity.Publisher;

@Entity
@Table(name = "MANGAS") 
public class Manga {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // Ánh xạ đúng vào cột manga_id
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
    
 // Thêm field này vào class Manga (cùng getter/setter)
    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    // Getter & Setter
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Publisher getPublisher() { return publisher; }
    public void setPublisher(Publisher publisher) { this.publisher = publisher; }
}