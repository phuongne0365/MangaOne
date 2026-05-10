package com.mangaone.repository;

import com.mangaone.entity.Manga;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MangaRepository extends JpaRepository<Manga, Long> {

    // Lọc truyện theo ID thể loại
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

    // [Trang chủ] Lấy top 5 truyện bán chạy (Giả định: tồn kho ít nhất là bán được nhiều nhất)
    @Query(value = "SELECT * FROM MANGAS ORDER BY stock_quantity ASC LIMIT 5", nativeQuery = true)
    List<Manga> findTopBestSellers();

    // [Quản trị] Tìm truyện sắp hết hàng (tồn kho dưới ngưỡng threshold)
    @Query("SELECT m FROM Manga m WHERE m.stockQuantity < :threshold ORDER BY m.stockQuantity ASC")
    List<Manga> findLowStockMangas(@Param("threshold") int threshold);
 
    // [Quản trị] Đếm số đầu truyện đã hết hàng (stock = 0)
    @Query("SELECT COUNT(m) FROM Manga m WHERE m.stockQuantity = 0")
    long countOutOfStock();
 
    // [Quản trị] Đếm số đầu truyện sắp hết (0 < stock < threshold)
    @Query("SELECT COUNT(m) FROM Manga m WHERE m.stockQuantity > 0 AND m.stockQuantity < :threshold")
    long countLowStock(@Param("threshold") int threshold);
}