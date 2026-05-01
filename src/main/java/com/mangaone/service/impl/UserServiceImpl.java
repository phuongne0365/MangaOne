package com.mangaone.service.impl;

import com.mangaone.entity.User;
import com.mangaone.repository.UserRepository;
import com.mangaone.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public String register(User user) {

        User existingUser = userRepository.findByEmail(user.getEmail());

        if (existingUser != null) {
            return "Email đã tồn tại!";
        }

        userRepository.save(user);

        return "Đăng ký thành công!";
    }

    @Override
    public User login(String email, String password) {

        User user = userRepository.findByEmail(email);

        if (user != null && user.getPassword().equals(password)) {
            return user;
        }

        return null;
    }
}