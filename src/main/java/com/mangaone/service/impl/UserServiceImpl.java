package com.mangaone.service.impl;

import com.mangaone.entity.User;
import com.mangaone.repository.UserRepository;
import com.mangaone.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public String register(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return "Email đã tồn tại!";
        }

        //  MÃ HÓA MẬT KHẨU
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "Đăng ký thành công!";
    }

    @Override
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email);

        //  SO SÁNH MẬT KHẨU BẰNG BCrypt
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }
}