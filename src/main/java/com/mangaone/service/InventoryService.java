package com.mangaone.service;

import com.mangaone.entity.Manga;
import java.util.List;

/**
 * Interface định nghĩa nghiệp vụ Quản lý Tồn kho.
 * Controller chỉ gọi qua interface này — không phụ thuộc vào cài đặt cụ thể.
 */
public interface InventoryService {

    /** Lấy toàn bộ danh sách truyện kèm tồn kho (trang Admin) */
    List<Manga> getAllMangasForAdmin();

    /** Cộng thêm số lượng vào kho (nhập hàng) */
    void nhapHang(Long mangaId, int soLuongNhap);

    /** Trừ đi số lượng khỏi kho (điều chỉnh giảm) */
    void xuatHang(Long mangaId, int soLuongXuat);

    /** Lấy danh sách truyện sắp hết hàng (stockQuantity < 5) */
    List<Manga> getSapHetHang();

    /** Đếm số đầu truyện hết hàng hoàn toàn */
    long demHetHang();

    /** Đếm số đầu truyện sắp hết */
    long demSapHetHang();

    /** Tổng số đầu truyện đang có */
    long demTongSoTruyen();
}
