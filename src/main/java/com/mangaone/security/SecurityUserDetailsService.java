package com.mangaone.security;

import com.mangaone.entity.User;
import com.mangaone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Cầu nối giữa User entity của dự án và Spring Security.
 * Spring Security gọi loadUserByUsername() khi xử lý POST /login.
 * "username" ở đây là email (vì form dùng field "username" nhưng ta map sang email).
 */
@Service
public class SecurityUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("Không tìm thấy email: " + email);
        }

        // Kiểm tra tài khoản bị khóa
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new UsernameNotFoundException("Tài khoản đã bị khóa: " + email);
        }

        // Chuyển role "ADMIN"/"USER" → GrantedAuthority "ROLE_ADMIN"/"ROLE_USER"
        String springRole = "ROLE_" + user.getRole(); // ROLE_ADMIN hoặc ROLE_USER

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())      // BCrypt hash lưu trong DB
                .authorities(new SimpleGrantedAuthority(springRole))
                .accountLocked(!Boolean.TRUE.equals(user.getIsActive()))
                .build();
    }
}
