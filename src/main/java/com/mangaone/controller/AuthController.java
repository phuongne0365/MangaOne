package com.mangaone.controller;

import com.mangaone.entity.User;
import com.mangaone.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    // ================= MỞ TRANG ĐĂNG KÝ =================
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // ================= XỬ LÝ LƯU ĐĂNG KÝ =================
    @PostMapping("/register")
    public String processRegister(@ModelAttribute("user") User user, Model model) {
        // Kiểm tra email trùng
        if (userRepository.findByEmail(user.getEmail()) != null) {
            model.addAttribute("error", "Email này đã được sử dụng!");
            return "register";
        }
        
        // Băm mật khẩu (Mã hóa) trước khi lưu
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashedPassword);
        
        userRepository.save(user); // Lưu xuống Database
        return "redirect:/login"; // Thành công thì chuyển sang trang đăng nhập
    }

    // ================= MỞ TRANG ĐĂNG NHẬP =================
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    // ================= XỬ LÝ KIỂM TRA ĐĂNG NHẬP =================
    @PostMapping("/login")
    public String processLogin(@RequestParam("email") String email, 
                               @RequestParam("password") String password, 
                               HttpSession session, Model model) {
        User user = userRepository.findByEmail(email);

        // So sánh mật khẩu người dùng nhập với mật khẩu đã mã hóa trong DB
        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            session.setAttribute("loggedInUser", user); // Cấp thẻ phiên làm việc (Session)
            return "redirect:/"; // Đăng nhập đúng thì về Trang chủ
        }

        model.addAttribute("error", "Sai email hoặc mật khẩu!");
        return "login";
    }

    // ================= XỬ LÝ ĐĂNG XUẤT =================
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("loggedInUser"); // Xóa thẻ phiên làm việc
        return "redirect:/";
    }
}