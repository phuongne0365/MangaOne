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
}
