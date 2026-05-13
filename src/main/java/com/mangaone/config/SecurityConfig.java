package com.mangaone.config;

import com.mangaone.security.CustomAuthenticationFailureHandler;
import com.mangaone.security.CustomAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    @Autowired
    private CustomAuthenticationFailureHandler failureHandler;

    /**
     * PasswordEncoder hỗ trợ mật khẩu PLAIN TEXT trong DB hiện tại.
     *
     * Vấn đề: DB đang lưu "123456" (không có prefix {noop} hay {bcrypt}).
     * DelegatingPasswordEncoder khi match cần đọc prefix {id} để biết dùng encoder nào.
     * Nếu không có prefix → phải set defaultPasswordEncoderForMatches = NoOpPasswordEncoder.
     *
     * Giải thích:
     *  - new DelegatingPasswordEncoder("noop", encoders) → chỉ quy định encoder KHI ENCODE mới
     *  - setDefaultPasswordEncoderForMatches()           → quy định encoder KHI MATCH (verify)
     *    khi password trong DB không có prefix
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("noop",   NoOpPasswordEncoder.getInstance()); // plain text có prefix {noop}
        encoders.put("bcrypt", new BCryptPasswordEncoder());       // BCrypt hash có prefix {bcrypt}

        DelegatingPasswordEncoder encoder = new DelegatingPasswordEncoder("noop", encoders);

        // KEY FIX: khi password trong DB không có prefix → coi là plain text
        encoder.setDefaultPasswordEncoderForMatches(NoOpPasswordEncoder.getInstance());

        return encoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Tắt CSRF để form POST hoạt động
            .csrf(csrf -> csrf.disable())

            // Cho phép tất cả request đi qua
            // (AdminController tự kiểm tra quyền bằng session.loggedInUser)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )

            // Cấu hình form login — Spring Security handle POST /login
            .formLogin(form -> form
                .usernameParameter("email")       // field email trong form
                .passwordParameter("password")
                .loginPage("/login")              // GET /login → AuthController
                .loginProcessingUrl("/login")     // POST /login → Spring Security
                .successHandler(successHandler)   // redirect thông minh sau login
                .failureHandler(failureHandler)    // xử lý lỗi cụ thể theo loại exception
                .permitAll()
            )

            // Tự xử lý logout trong AuthController (@GetMapping("/logout"))
            .logout(logout -> logout.disable());

        return http.build();
    }
}