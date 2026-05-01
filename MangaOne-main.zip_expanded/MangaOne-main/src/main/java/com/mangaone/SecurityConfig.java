package com.mangaone;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Tắt CSRF để form POST hoạt động
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Cho phép tất cả request đi qua
            )
            .formLogin(form -> form.disable()) // Tắt trang login mặc định của Spring
            .logout(logout -> logout.disable()); // Bạn tự xử lý logout bằng session

        return http.build();
    }
}