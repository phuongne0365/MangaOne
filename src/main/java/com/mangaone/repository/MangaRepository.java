package com.mangaone.repository;

import com.mangaone.entity.Manga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MangaRepository extends JpaRepository<Manga, Long> {
    // Không cần viết gì thêm, JpaRepository đã có sẵn các hàm Save, FindAll, Delete...
}