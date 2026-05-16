package com.mangaone.config;

import com.mangaone.security.CustomAuthenticationFailureHandler;
import com.mangaone.security.CustomAuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    @Autowired
    private CustomAuthenticationFailureHandler failureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Chỉ các route /admin/** và /dashboard mới cần ROLE_ADMIN
                .requestMatchers(
                    "/admin/**",
                    "/dashboard"
                ).hasRole("ADMIN")
                // Tất cả các trang còn lại cho phép truy cập tự do
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .usernameParameter("email")
                .passwordParameter("password")
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll()
            )
            // Khi không có quyền (403) → redirect về trang chủ và mở popup login
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendRedirect(request.getContextPath() + "/?openLogin=true")
                )
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    response.sendRedirect(request.getContextPath() + "/")
                )
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }
}