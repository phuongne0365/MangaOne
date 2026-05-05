package com.mangaone.service;

import com.mangaone.entity.User;

public interface UserService {

    String register(User user);

    User login(String email, String password);

}
