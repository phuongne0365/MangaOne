package com.mangaone.repository;

import com.mangaone.entity.Manga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MangaRepository extends JpaRepository<Manga, Long> {
    // Để trống như này là đủ, JpaRepository sẽ tự lo hết các hàm tìm kiếm, lưu, xóa.
}