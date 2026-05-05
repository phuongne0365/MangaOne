package com.mangaone.repository;

import com.mangaone.entity.Manga;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MangaRepository extends JpaRepository<Manga, Long> {

    // Hàm cũ của nhóm - giữ nguyên
    List<Manga> findByCategory_CategoryId(Long categoryId);

    // Tìm kiếm theo từ khóa (tên truyện hoặc tác giả) - không phân biệt hoa thường
    @Query("SELECT m FROM Manga m WHERE " +
           "LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Manga> searchByKeyword(@Param("keyword") String keyword);

    // Tìm kiếm theo từ khóa VÀ lọc theo thể loại cùng lúc
    @Query("SELECT m FROM Manga m WHERE " +
           "(LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "m.category.categoryId = :categoryId")
    List<Manga> searchByKeywordAndCategory(@Param("keyword") String keyword,
                                           @Param("categoryId") Long categoryId);
}