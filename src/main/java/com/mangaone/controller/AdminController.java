package com.mangaone.controller;

import com.mangaone.entity.User;
import com.mangaone.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    // 👥 TRANG RIÊNG BIỆT: GET /admin/users
    @GetMapping("/users")
    public String listUsers(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";

        model.addAttribute("currentPage", "users"); // Sáng riêng đèn "Quản lý thành viên"
        model.addAttribute("currentUser", session.getAttribute("loggedInUser"));

        List<User> allUsers = userRepository.findAll();
        long hoatDong  = allUsers.stream().filter(u -> Boolean.TRUE.equals(u.getIsActive())).count();
        long biBiKhoa  = allUsers.stream().filter(u -> !Boolean.TRUE.equals(u.getIsActive())).count();
        long adminCount = allUsers.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
        
        model.addAttribute("users", allUsers);
        model.addAttribute("tongThanhVien",     allUsers.size());
        model.addAttribute("thanhVienHoatDong", hoatDong);
        model.addAttribute("thanhVienBiKhoa",   biBiKhoa);
        model.addAttribute("soQuanTriVien",     adminCount);

        return "admin/admin-users"; // Trả về file giao diện riêng của nhóm Duy
    }

    @PostMapping("/users/toggle-active")
    public String toggleActive(@RequestParam("userId") Long userId, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            User me = (User) session.getAttribute("loggedInUser");
            if (!user.getUserId().equals(me.getUserId())) {
                user.setIsActive(!user.getIsActive());
                userRepository.save(user);
            }
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/toggle-role")
    public String toggleRole(@RequestParam("userId") Long userId, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            User me = (User) session.getAttribute("loggedInUser");
            if (!user.getUserId().equals(me.getUserId())) {
                user.setRole("ADMIN".equals(user.getRole()) ? "USER" : "ADMIN");
                userRepository.save(user);
            }
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete")
    public String softDelete(@RequestParam("userId") Long userId, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            User me = (User) session.getAttribute("loggedInUser");
            if (!user.getUserId().equals(me.getUserId())) {
                user.setIsActive(false);
                userRepository.save(user);
            }
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/update")
    @ResponseBody
    public Map<String, Object> apiUpdateUser(@RequestParam("userId") Long userId, @RequestParam("role") String role, @RequestParam("isActive") boolean isActive, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        if (!isAdmin(session)) { response.put("success", false); return response; }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) { response.put("success", false); return response; }

        User me = (User) session.getAttribute("loggedInUser");
        if (user.getUserId().equals(me.getUserId()) && (!isActive || !"ADMIN".equals(role))) {
            response.put("success", false);
            response.put("message", "Không thể tự hạ quyền hoặc tự khóa chính mình!");
            return response;
        }

        user.setRole(role); user.setIsActive(isActive); userRepository.save(user);
        response.put("success", true); response.put("role", user.getRole()); response.put("isActive", user.getIsActive());
        return response;
    }
}