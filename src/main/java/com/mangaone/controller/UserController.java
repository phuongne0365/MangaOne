package com.mangaone.controller;

import com.mangaone.entity.User;
import com.mangaone.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
@Controller
public class UserController {
	@Autowired
	private PasswordEncoder passwordEncoder; // Thêm dòng này để dùng được BCrypt
    @Autowired
    private UserRepository userRepository;

    // ===== TRANG THÔNG TIN CÁ NHÂN =====
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User loggedIn = (User) session.getAttribute("loggedInUser");
        if (loggedIn == null) return "redirect:/login";

        // Lấy thông tin mới nhất từ DB
        User user = userRepository.findById(loggedIn.getUserId()).orElse(null);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        return "profile";
    }

    // ===== CẬP NHẬT THÔNG TIN CÁ NHÂN =====
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("fullName")    String fullName,
                                @RequestParam("phoneNumber") String phoneNumber,
                                @RequestParam("address")     String address,
                                HttpSession session, Model model) {
        User loggedIn = (User) session.getAttribute("loggedInUser");
        if (loggedIn == null) return "redirect:/login";

        User user = userRepository.findById(loggedIn.getUserId()).orElse(null);
        if (user == null) return "redirect:/login";

        user.setFullName(fullName);
        user.setPhoneNumber(phoneNumber);
        user.setAddress(address);
        userRepository.save(user);

        // Cập nhật session
        session.setAttribute("loggedInUser", user);
        model.addAttribute("user", user);
        model.addAttribute("success", "Cập nhật thông tin thành công!");
        return "profile";
    }

    // ===== ĐỔI MẬT KHẨU =====
    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session, Model model) {
        User loggedIn = (User) session.getAttribute("loggedInUser");
        if (loggedIn == null) return "redirect:/login";

        User user = userRepository.findById(loggedIn.getUserId()).orElse(null);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);

        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            model.addAttribute("pwError", "Mật khẩu cũ không chính xác!");
            return "profile";
        }

        // Kiểm tra mật khẩu mới khớp nhau
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("pwError", "Mật khẩu mới không khớp!");
            return "profile";
        }

        // Kiểm tra độ dài
        if (newPassword.length() < 6) {
            model.addAttribute("pwError", "Mật khẩu mới phải có ít nhất 6 ký tự!");
            return "profile";
        }

        user.setPassword(passwordEncoder.encode(newPassword)); 
        userRepository.save(user);
        session.setAttribute("loggedInUser", user);
        model.addAttribute("pwSuccess", "Đổi mật khẩu thành công!");
        return "profile";
    }
}