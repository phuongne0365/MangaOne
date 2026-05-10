package com.mangaone.service;

import com.mangaone.entity.User;
import com.mangaone.repository.UserRepository;
import com.mangaone.service.impl.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho tầng Service: UserServiceImpl — Chức năng Đăng nhập
 *
 * Phạm vi kiểm thử : Logic nghiệp vụ tại tầng Service (hàm login).
 * Cô lập hoàn toàn : UserRepository được Mock bằng Mockito.
 *                    KHÔNG kết nối cơ sở dữ liệu thật.
 *                    KHÔNG khởi động Spring Application Context.
 *                    KHÔNG sử dụng bất kỳ thư viện kiểm thử giao diện nào
 *                    (Selenium, Playwright, ...) — không có thao tác refresh.
 *
 * Test Case 1 : Đăng nhập thành công — email & mật khẩu đúng.
 * Test Case 2a: Đăng nhập thất bại  — tài khoản không tồn tại (email sai).
 * Test Case 2b: Đăng nhập thất bại  — email đúng nhưng sai mật khẩu.
 */
@ExtendWith(MockitoExtension.class)   // Kích hoạt Mockito với JUnit 5
class UserServiceImplTest {

    // SETUP: Khai báo Mock và đối tượng cần kiểm thử

    @Mock
    private UserRepository userRepository;

    /**
     * Đối tượng cần kiểm thử.
     * @InjectMocks tự động tiêm mockUserRepository vào userService qua field injection.
     */
    @InjectMocks
    private UserServiceImpl userService;

    // Dữ liệu dùng chung cho tất cả test case
    private static final String EMAIL_HOP_LE    = "tanaka@mangaone.com";
    private static final String MAT_KHAU_DUNG   = "Manga@2025";
    private static final String MAT_KHAU_SAI    = "SaiMatKhau999";
    private static final String EMAIL_KHONG_TON_TAI = "khongtontai@mangaone.com";

    private User userMau;

    @BeforeEach
    void chuanBiDuLieu() {
        // Tạo đối tượng User mẫu đại diện cho một tài khoản hợp lệ đã có trong DB
        userMau = new User();
        userMau.setUserId(1L);
        userMau.setEmail(EMAIL_HOP_LE);
        userMau.setPassword(MAT_KHAU_DUNG);     
        userMau.setFullName("Tanaka Hiroshi");
        userMau.setRole("USER");
        userMau.setIsActive(true);
    }

    // TEST CASE 1: Đăng nhập THÀNH CÔNG

    /**
     * Kịch bản: Người dùng nhập đúng cả email lẫn mật khẩu.
     */
    @Test
    @DisplayName("TC1 - Đăng nhập thành công: email và mật khẩu hợp lệ → trả về đối tượng User")
    void dangNhap_ThanhCong_KhiEmailVaMatKhauDung() {
        // ---- GIVEN: Chuẩn bị ------------------------------------------------
        // Định nghĩa hành vi mock:
        // findByEmail(EMAIL_HOP_LE) → trả về userMau (giả lập tìm thấy trong DB)
        when(userRepository.findByEmail(EMAIL_HOP_LE)).thenReturn(userMau);

        // ---- WHEN: Thực thi --------------------------------------------------
        User ketQua = userService.login(EMAIL_HOP_LE, MAT_KHAU_DUNG);

        // ---- THEN: Xác nhận --------------------------------------------------
        // 1) Kết quả phải KHÁC null — đăng nhập thành công phải trả về User
        assertNotNull(ketQua,
                "Đăng nhập thành công phải trả về đối tượng User, không được null");

        // 2) Email trong kết quả phải khớp với email đã đăng nhập
        assertEquals(EMAIL_HOP_LE, ketQua.getEmail(),
                "Email của User trả về phải trùng với email đã nhập");

        // 3) Tên đầy đủ phải đúng — xác nhận đúng user được trả về, không phải user lạ
        assertEquals("Tanaka Hiroshi", ketQua.getFullName(),
                "Họ tên phải khớp với dữ liệu user trong hệ thống");

        // 4) Role phải là "USER"
        assertEquals("USER", ketQua.getRole(),
                "Role của user thông thường phải là USER");

        // 5) Tài khoản phải đang active
        assertTrue(ketQua.getIsActive(),
                "Tài khoản vừa đăng nhập thành công phải đang ở trạng thái active");

        // 6) Xác nhận findByEmail được gọi đúng 1 lần với email chính xác
        verify(userRepository, times(1)).findByEmail(EMAIL_HOP_LE);

        // 7) Không có lời gọi ngoài ý muốn nào tới repository (không gọi save, delete, ...)
        verifyNoMoreInteractions(userRepository);
    }

    // TEST CASE 2a: Đăng nhập THẤT BẠI — Tài khoản không tồn tại

    /**
     * Kịch bản: Người dùng nhập một email chưa được đăng ký trong hệ thống.
     */
    @Test
    @DisplayName("TC2a - Đăng nhập thất bại: email không tồn tại → trả về null")
    void dangNhap_ThatBai_KhiEmailKhongTonTai() {
        // ---- GIVEN: Chuẩn bị ------------------------------------------------
        // Repository "không tìm thấy" user nào với email này → trả về null
        when(userRepository.findByEmail(EMAIL_KHONG_TON_TAI)).thenReturn(null);

        // ---- WHEN: Thực thi --------------------------------------------------
        User ketQua = userService.login(EMAIL_KHONG_TON_TAI, MAT_KHAU_DUNG);

        // ---- THEN: Xác nhận --------------------------------------------------
        // 1) Kết quả PHẢI là null — không có user nào được trả về khi email sai
        assertNull(ketQua,
                "Khi email không tồn tại trong hệ thống, login() phải trả về null");

        // 2) Xác nhận findByEmail được gọi đúng 1 lần
        verify(userRepository, times(1)).findByEmail(EMAIL_KHONG_TON_TAI);

        // 3) Không có lời gọi nào khác (đặc biệt không gọi save hay bất kỳ mutation nào)
        verifyNoMoreInteractions(userRepository);
    }

    // TEST CASE 2b: Đăng nhập THẤT BẠI — Email đúng nhưng sai mật khẩu

    /**
     * Kịch bản: Email có tồn tại trong hệ thống, nhưng người dùng nhập sai mật khẩu.
     */
    @Test
    @DisplayName("TC2b - Đăng nhập thất bại: email đúng nhưng sai mật khẩu → trả về null")
    void dangNhap_ThatBai_KhiSaiMatKhau() {
        // ---- GIVEN: Chuẩn bị ------------------------------------------------
        // Repository tìm thấy user theo email — nhưng mật khẩu sẽ không khớp
        when(userRepository.findByEmail(EMAIL_HOP_LE)).thenReturn(userMau);

        // ---- WHEN: Thực thi --------------------------------------------------
        // Truyền đúng email nhưng SAI mật khẩu
        User ketQua = userService.login(EMAIL_HOP_LE, MAT_KHAU_SAI);

        // ---- THEN: Xác nhận --------------------------------------------------
        // 1) Kết quả PHẢI là null — sai mật khẩu không được phép đăng nhập
        assertNull(ketQua,
                "Khi mật khẩu không khớp, login() phải trả về null, không được trả về User");

        // 2) Xác nhận findByEmail đã được gọi đúng 1 lần
        //    (Service vẫn phải tra cứu DB để biết user tồn tại, trước khi so mật khẩu)
        verify(userRepository, times(1)).findByEmail(EMAIL_HOP_LE);

        // 3) Không có lời gọi nào khác tới repository
        verifyNoMoreInteractions(userRepository);
    }
}
