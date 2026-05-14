package com.mangaone.controller;

import com.mangaone.entity.User;
import com.mangaone.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;  // ✨ INJECT PasswordEncoder

    // ================= MỞ TRANG ĐĂNG KÝ =================
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "redirect:/?openLogin=true";
    }

    // ================= XỬ LÝ LƯU ĐĂNG KÝ =================
    @PostMapping("/register")
    public String processRegister(@ModelAttribute("user") User user, Model model, HttpSession session) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            session.setAttribute("registerError", "Email này đã được sử dụng!");
            return "redirect:/?openRegister=true";
        }

        // ✨ MÃ HÓA MẬT KHẨU TRƯỚC KHI LƯU
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        userRepository.save(user);
        session.setAttribute("registerSuccess", "Đăng ký thành công! Hãy đăng nhập.");
        return "redirect:/?openLogin=true";
    }

    // ================= MỞ TRANG ĐĂNG NHẬP =================
    // LƯU Ý: POST /login được Spring Security handle tự động.
    // GET /login chỉ hiển thị trang và lưu lại trang trước đó vào session.
    @GetMapping("/login")
    public String showLoginForm(HttpServletRequest request,
            HttpSession session,
            Model model,
            @RequestParam(value = "loginError", required = false) String loginErrorParam) {

        // Lưu Referer để sau khi login có thể quay lại trang cũ
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank() && !referer.contains("/login")) {
            session.setAttribute("PREVIOUS_URL", referer);
        }

        // Đọc thông báo lỗi từ CustomAuthenticationFailureHandler
        // (trường hợp failureHandler redirect về /?loginError=... nhưng browser vào
        // /login trực tiếp)
        if (loginErrorParam != null && !loginErrorParam.isBlank()) {
            model.addAttribute("loginError", loginErrorParam);
        }

        // Đọc thông báo đăng ký thành công từ session (nếu có)
        if (session.getAttribute("registerSuccess") != null) {
            model.addAttribute("registerSuccess", session.getAttribute("registerSuccess"));
            session.removeAttribute("registerSuccess");
        }

        return "redirect:/?openRegister=true";
    }

    // ================= XỬ LÝ ĐĂNG XUẤT =================
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("loggedInUser"); // Xóa user khỏi session của dự án
        session.removeAttribute("PREVIOUS_URL"); // Dọn dẹp
        SecurityContextHolder.clearContext(); // Xóa context của Spring Security
        session.invalidate(); // Hủy toàn bộ session
        return "redirect:/";
    }
}