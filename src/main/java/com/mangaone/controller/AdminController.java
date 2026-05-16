package com.mangaone.controller;

import com.mangaone.entity.User;
import com.mangaone.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return "redirect:/admin/dashboard#section-thanh-vien";
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
        return "redirect:/admin/dashboard#section-thanh-vien";
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
        return "redirect:/admin/dashboard#section-thanh-vien";
    }
    // ===== CẬP NHẬT TỔNG HỢP (AJAX) =====
    @PostMapping("/admin/users/update")
    @ResponseBody
    public java.util.Map<String, Object> updateUser(
            @RequestParam("userId") Long userId,
            @RequestParam("role") String role,
            @RequestParam("isActive") boolean isActive,
            HttpSession session) {

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        User me = (User) session.getAttribute("loggedInUser");

        if (me == null || !"ADMIN".equals(me.getRole())) {
            result.put("success", false);
            result.put("message", "Bạn không có quyền thực hiện thao tác này.");
            return result;
        }

        // Chặn self-demotion: không cho tự hạ quyền hoặc tự khóa chính mình
        if (me.getUserId().equals(userId)) {
            if (!isActive) {
                result.put("success", false);
                result.put("message", "Bạn không thể tự khóa tài khoản của chính mình!");
                return result;
            }
            if (!"ADMIN".equals(role)) {
                result.put("success", false);
                result.put("message", "Bạn không thể tự hạ quyền của chính mình!");
                return result;
            }
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            result.put("success", false);
            result.put("message", "Không tìm thấy tài khoản.");
            return result;
        }

        user.setRole(role);
        user.setIsActive(isActive);
        userRepository.save(user);

        result.put("success", true);
        result.put("message", "Cập nhật thành công!");
        result.put("role", user.getRole());
        result.put("isActive", user.getIsActive());
        return result;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";

        User user = (User) session.getAttribute("loggedInUser");
        model.addAttribute("currentUser", user);

        // Thêm các link menu admin
        model.addAttribute("adminMenus", new String[][]{
                {"/admin/users", "👥 Quản Lý Người Dùng"},
                {"/admin/publishers", "📚 Quản Lý Nhà Xuất Bản"},
                {"/admin/categories", "📂 Quản Lý Thể Loại"},
                {"/admin/mangas", "🎨 Quản Lý Manga"},
                {"/admin/inventory", "📦 Quản Lý Kho"}
        });

        return "admin/dashboard";
    }
}