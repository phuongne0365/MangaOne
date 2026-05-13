package com.mangaone.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Xử lý khi đăng nhập thất bại.
 * Phân loại từng loại lỗi để hiển thị thông báo cụ thể trong modal.
 */
@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {

        String errorMessage;

        if (exception instanceof BadCredentialsException) {
            // Sai mật khẩu (email đúng nhưng password sai)
            errorMessage = "Mật khẩu không đúng. Vui lòng thử lại!";

        } else if (exception instanceof UsernameNotFoundException) {
            // Không tìm thấy email trong DB
            errorMessage = "Email này chưa được đăng ký!";

        } else if (exception instanceof LockedException) {
            // Tài khoản bị khóa (isActive = false)
            errorMessage = "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên!";

        } else if (exception instanceof DisabledException) {
            // Tài khoản bị vô hiệu hóa
            errorMessage = "Tài khoản của bạn đã bị vô hiệu hóa!";

        } else if (exception instanceof AccountExpiredException) {
            // Tài khoản hết hạn
            errorMessage = "Tài khoản đã hết hạn!";

        } else {
            // Lỗi chung (không xác định)
            errorMessage = "Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin!";
        }

        // Encode URL để tránh lỗi ký tự đặc biệt
        String encodedError = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);

        // Redirect về trang chủ, mở modal login và hiển thị lỗi cụ thể
        getRedirectStrategy().sendRedirect(request, response,
                "/?loginError=" + encodedError);
    }
}
