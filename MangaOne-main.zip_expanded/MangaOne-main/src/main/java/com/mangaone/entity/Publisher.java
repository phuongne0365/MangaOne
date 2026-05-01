package com.mangaone.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "PUBLISHERS")
public class Publisher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "publisher_id")
    private Integer publisherId;

    @Column(name = "publisher_name", nullable = false, length = 150)
    private String publisherName;

    // Quan hệ ngược: Một Publisher có nhiều Manga
    @OneToMany(mappedBy = "publisher", fetch = FetchType.LAZY)
    private List<Manga> mangas;
    public Integer getPublisherId() { return publisherId; }
    public void setPublisherId(Integer publisherId) { this.publisherId = publisherId; }

    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }

    public List<Manga> getMangas() { return mangas; }
    public void setMangas(List<Manga> mangas) { this.mangas = mangas; }
}
