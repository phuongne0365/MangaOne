package com.mangaone.controller;

import com.mangaone.entity.Order; 
import com.mangaone.entity.User;
import com.mangaone.repository.OrderRepository;
import com.mangaone.repository.UserRepository;
import com.mangaone.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List; // ĐÃ FIX: Thêm import List

@Controller
public class UserController {

    @Autowired
    private PasswordEncoder passwordEncoder; // Dùng BCrypt xử lý mật khẩu

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderService orderService; // Inject tầng Service để gọi hàm hủy đơn của nhóm
    
    @Autowired
    private OrderRepository orderRepository;

    // ===== TRANG THÔNG TIN CÁ NHÂN =====
    @GetMapping("/profile")
    @Transactional  // ✨ Giữ session mở để load lazy-loaded orders
    public String profile(HttpSession session, Model model) {
        User loggedIn = (User) session.getAttribute("loggedInUser");
        if (loggedIn == null) return "redirect:/login";

        // Lấy thông tin mới nhất từ DB
        User user = userRepository.findById(loggedIn.getUserId()).orElse(null);
        if (user == null) return "redirect:/login";
        
        // ĐÃ FIX: Lấy danh sách đơn hàng mới nhất và gán vào Object User để Thymeleaf hiển thị ra bảng
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        user.setOrders(orders);
        
        model.addAttribute("user", user);
        return "profile";
    }

    // ===== CẬP NHẬT THÔNG TIN CÁ NHÂN =====
    @PostMapping("/profile/update")
    @Transactional  // ✨ Giữ session mở để load lazy-loaded orders
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

        // Cập nhật session và load lại danh sách đơn hàng tránh lỗi hiển thị trống
        session.setAttribute("loggedInUser", user);
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        user.setOrders(orders);

        model.addAttribute("user", user);
        model.addAttribute("success", "Cập nhật thông tin thành công!");
        return "profile";
    }

    // ===== ĐỔI MẬT KHẨU =====
    @PostMapping("/profile/change-password")
    @Transactional  // ✨ Giữ session mở để load lazy-loaded orders
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session, Model model) {
        User loggedIn = (User) session.getAttribute("loggedInUser");
        if (loggedIn == null) return "redirect:/login";

        User user = userRepository.findById(loggedIn.getUserId()).orElse(null);
        if (user == null) return "redirect:/login";

        // Load lại danh sách đơn hàng cho user tránh mất dữ liệu bảng khi trả về trang lỗi/thành công
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        user.setOrders(orders);
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

    // ===== XỬ LÝ HỦY ĐƠN HÀNG =====
    @PostMapping("/order/cancel")
    public String handleCancelOrder(@RequestParam("orderId") Long orderId, RedirectAttributes redirectAttributes, HttpSession session) {
        User loggedIn = (User) session.getAttribute("loggedInUser");
        if (loggedIn == null) return "redirect:/login";

        try {
            // Gọi hàm xử lý logic hủy và hoàn kho của nhóm 
            orderService.cancelOrder(orderId);
            redirectAttributes.addFlashAttribute("success", "Hủy đơn hàng thành công! Số lượng truyện đã được hoàn lại vào kho hàng.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/profile";
    }
}