package com.mangaone.repository;

import com.mangaone.entity.Manga;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MangaRepository extends JpaRepository<Manga, Long> {

    // Lọc truyện theo ID thể loại - Thêm EntityGraph để nạp kèm luôn thông tin liên kết trong 1 câu lệnh
    @EntityGraph(attributePaths = {"category", "publisher"})
    List<Manga> findByCategory_CategoryId(Long categoryId);

    // Tìm kiếm theo từ khóa (tên truyện hoặc tác giả) - không phân biệt hoa thường
    @EntityGraph(attributePaths = {"category", "publisher"})
    @Query("SELECT m FROM Manga m WHERE " +
           "LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Manga> searchByKeyword(@Param("keyword") String keyword);

    // Tìm kiếm theo từ khóa VÀ lọc theo thể loại cùng lúc
    @EntityGraph(attributePaths = {"category", "publisher"})
    @Query("SELECT m FROM Manga m WHERE " +
           "(LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "m.category.categoryId = :categoryId")
    List<Manga> searchByKeywordAndCategory(@Param("keyword") String keyword,
                                           @Param("categoryId") Long categoryId);

    
    
    // [Trang chủ] Lấy top 5 truyện bán chạy (Sắp xếp tồn kho tăng dần)
    // Sử dụng cơ chế đặt tên hàm của Spring Data JPA kết hợp @EntityGraph để gộp bảng (JOIN) siêu tốc
    @EntityGraph(attributePaths = {"category", "publisher"})
    List<Manga> findTop5ByOrderByStockQuantityAsc();

    // [Quản trị] Tìm truyện sắp hết hàng (tồn kho dưới ngưỡng threshold)
    @EntityGraph(attributePaths = {"category", "publisher"})
    @Query("SELECT m FROM Manga m WHERE m.stockQuantity < :threshold ORDER BY m.stockQuantity ASC")
    List<Manga> findLowStockMangas(@Param("threshold") int threshold);
 
    // [Quản trị] Đếm số đầu truyện đã hết hàng (stock = 0) - Không cần nạp liên kết vì chỉ lấy số lượng
    @Query("SELECT COUNT(m) FROM Manga m WHERE m.stockQuantity = 0")
    long countOutOfStock();
 
    // [Quản trị] Đếm số đầu truyện sắp hết (0 < stock < threshold)
    @Query("SELECT COUNT(m) FROM Manga m WHERE m.stockQuantity > 0 AND m.stockQuantity < :threshold")
    long countLowStock(@Param("threshold") int threshold);
}