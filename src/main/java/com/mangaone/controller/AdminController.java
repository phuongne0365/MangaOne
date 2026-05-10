package com.mangaone.controller;

import com.mangaone.entity.User;
import com.mangaone.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    // ===== KIỂM TRA QUYỀN ADMIN =====
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    // ===== TRANG DANH SÁCH THÀNH VIÊN =====
    @GetMapping("/admin/users")
    public String listUsers(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";

        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("currentUser", session.getAttribute("loggedInUser"));
        return "admin-users";
    }

    // ===== KHÓA / MỞ KHÓA TÀI KHOẢN =====
    @PostMapping("/admin/users/toggle-active")
    public String toggleActive(@RequestParam("userId") Long userId,
                               HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";

        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            // Không cho khóa chính mình
            User me = (User) session.getAttribute("loggedInUser");
            if (!user.getUserId().equals(me.getUserId())) {
                user.setIsActive(!user.getIsActive());
                userRepository.save(user);
            }
        }
        return "redirect:/admin/users";
    }

    // ===== THAY ĐỔI VAI TRÒ USER / ADMIN =====
    @PostMapping("/admin/users/toggle-role")
    public String toggleRole(@RequestParam("userId") Long userId,
                             HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";

        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            // Không cho đổi role của chính mình
            User me = (User) session.getAttribute("loggedInUser");
            if (!user.getUserId().equals(me.getUserId())) {
                if ("ADMIN".equals(user.getRole())) {
                    user.setRole("USER");
                } else {
                    user.setRole("ADMIN");
                }
                userRepository.save(user);
            }
        }
        return "redirect:/admin/users";
    }

    // ===== XÓA MỀM TÀI KHOẢN =====
    @PostMapping("/admin/users/delete")
    public String softDelete(@RequestParam("userId") Long userId,
                             HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";

        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            User me = (User) session.getAttribute("loggedInUser");
            if (!user.getUserId().equals(me.getUserId())) {
                // Xóa mềm = khóa tài khoản và đánh dấu bị xóa
                user.setIsActive(false);
                userRepository.save(user);
            }
        }
        return "redirect:/admin/users";
    }
    
}