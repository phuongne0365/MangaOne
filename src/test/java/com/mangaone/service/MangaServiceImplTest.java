package com.mangaone.service;

import com.mangaone.entity.Category;
import com.mangaone.entity.Manga;
import com.mangaone.repository.MangaRepository;
import com.mangaone.service.impl.MangaServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho tầng Service: MangaServiceImpl
 *
 * Phạm vi kiểm thử: Chỉ kiểm tra logic nghiệp vụ tại tầng Service.
 * Tầng Repository được Mock hoàn toàn bằng Mockito — KHÔNG kết nối CSDL thật.
 * KHÔNG sử dụng bất kỳ thư viện kiểm thử giao diện (Selenium, Playwright, ...).
 *
 * Test Case 1: Thêm mới một cuốn truyện vào kho thành công
 * Test Case 2: Lọc danh sách truyện theo Thể loại (categoryId hợp lệ)
 */
@ExtendWith(MockitoExtension.class)  // Kích hoạt Mockito với JUnit 5
class MangaServiceImplTest {

    // SETUP: Khai báo Mock và đối tượng cần kiểm thử

    @Mock
    private MangaRepository mangaRepository;

    /**
     * Đối tượng MangaServiceImpl thực sự cần kiểm thử.
     * @InjectMocks sẽ tự động tiêm mockMangaRepository vào đây.
     */
    @InjectMocks
    private MangaServiceImpl mangaService;

    // Dữ liệu dùng chung cho các test case
    private Manga mangaMau;
    private Category categoryMau;

    @BeforeEach
    void chuanBiDuLieu() {
        // Tạo đối tượng Category mẫu 
        categoryMau = new Category();
        categoryMau.setCategoryId(1);
        categoryMau.setCategoryName("Hành Động");

        // Tạo đối tượng Manga mẫu dùng chung trong các test
        mangaMau = new Manga();
        mangaMau.setTitle("Naruto");
        mangaMau.setAuthor("Masashi Kishimoto");
        mangaMau.setPrice(45000.0);
        mangaMau.setStockQuantity(100);
        mangaMau.setCategory(categoryMau);
        mangaMau.setDescription("Truyện về ninja làng Lá");
    }

    // TEST CASE 1: Thêm mới một cuốn truyện vào kho thành công

    @Test
    @DisplayName("TC1 - Thêm mới truyện thành công: saveManga() phải gọi repository.save() đúng 1 lần")
    void themMoiTruyen_ThanhCong() {
        // ---- GIVEN: Chuẩn bị ------------------------------------------------
        // Mockito: khi repository.save() được gọi với bất kỳ Manga nào,
        // trả về chính đối tượng đó (giả lập hành vi JPA thông thường).
        when(mangaRepository.save(any(Manga.class))).thenReturn(mangaMau);

        // ---- WHEN: Thực thi --------------------------------------------------
        // Gọi đúng phương thức Service cần kiểm thử
        mangaService.saveManga(mangaMau);

        // ---- THEN: Xác nhận --------------------------------------------------
        // 1) Xác nhận repository.save() đã được gọi ĐÚNG 1 LẦN với đối tượng mangaMau
        verify(mangaRepository, times(1)).save(mangaMau);

        // 2) Xác nhận KHÔNG có phương thức nào khác của repository bị gọi ngoài ý muốn
        verifyNoMoreInteractions(mangaRepository);
    }

    
}
