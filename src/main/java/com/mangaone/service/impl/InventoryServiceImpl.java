package com.mangaone.service.impl;

import com.mangaone.entity.Manga;
import com.mangaone.repository.MangaRepository;
import com.mangaone.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Transactional đảm bảo mọi thay đổi số lượng đều an toàn:
 * nếu có lỗi giữa chừng → Spring tự ROLLBACK, không để DB ở trạng thái nửa vời.
 */
@Service
public class InventoryServiceImpl implements InventoryService {

    // Ngưỡng cảnh báo sắp hết hàng — đặt hằng số dễ thay đổi
    private static final int NGUONG_SAP_HET = 5;

    private final MangaRepository mangaRepository;

    public InventoryServiceImpl(MangaRepository mangaRepository) {
        this.mangaRepository = mangaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Manga> getAllMangasForAdmin() {
        return mangaRepository.findAll();
    }

    /**
     * NHẬP HÀNG: Admin nhập số lượng → hàm này cộng vào stockQuantity hiện tại.
     */
    @Override
    @Transactional
    public void nhapHang(Long mangaId, int soLuongNhap) {
        if (soLuongNhap <= 0) {
            throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0.");
        }
        Manga manga = mangaRepository.findById(mangaId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy truyện ID: " + mangaId));

        manga.setStockQuantity(manga.getStockQuantity() + soLuongNhap);
        mangaRepository.save(manga);
    }

    /**
     * XUẤT HÀNG / ĐIỀU CHỈNH GIẢM.
     * Kiểm tra không được xuất quá số lượng tồn hiện có.
     */
    @Override
    @Transactional
    public void xuatHang(Long mangaId, int soLuongXuat) {
        if (soLuongXuat <= 0) {
            throw new IllegalArgumentException("Số lượng xuất phải lớn hơn 0.");
        }
        Manga manga = mangaRepository.findById(mangaId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy truyện ID: " + mangaId));

        int tonKhoMoi = manga.getStockQuantity() - soLuongXuat;
        if (tonKhoMoi < 0) {
            throw new IllegalStateException(
                    "Không đủ hàng! Tồn kho hiện tại: " + manga.getStockQuantity());
        }
        manga.setStockQuantity(tonKhoMoi);
        mangaRepository.save(manga);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Manga> getSapHetHang() {
        return mangaRepository.findLowStockMangas(NGUONG_SAP_HET);
    }

    @Override
    @Transactional(readOnly = true)
    public long demHetHang() {
        return mangaRepository.countOutOfStock();
    }

    @Override
    @Transactional(readOnly = true)
    public long demSapHetHang() {
        return mangaRepository.countLowStock(NGUONG_SAP_HET);
    }

    @Override
    @Transactional(readOnly = true)
    public long demTongSoTruyen() {
        return mangaRepository.count();
    }
}
