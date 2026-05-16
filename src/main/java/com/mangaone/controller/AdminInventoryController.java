package com.mangaone.controller;

import com.mangaone.entity.User;
import com.mangaone.entity.Manga;
import com.mangaone.service.InventoryService;
import com.mangaone.repository.OrderRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminInventoryController {

    private final InventoryService inventoryService;
    private final OrderRepository orderRepository;

    public AdminInventoryController(InventoryService inventoryService, OrderRepository orderRepository) {
        this.inventoryService = inventoryService;
        this.orderRepository = orderRepository;
    }

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    @GetMapping
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    // 📊 TRANG DASHBOARD CHỈ CHỨA THỐNG KÊ KHO & BIỂU ĐỒ DOANH THU
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";

        User user = (User) session.getAttribute("loggedInUser");
        model.addAttribute("currentUser", user);
        model.addAttribute("currentPage", "dashboard"); // Chỉ sáng 1 đèn Dashboard

        // Thống kê Kho hàng
        model.addAttribute("tongSoTruyen",  inventoryService.demTongSoTruyen());
        model.addAttribute("soHetHang",     inventoryService.demHetHang());
        model.addAttribute("soSapHetHang",  inventoryService.demSapHetHang());
        model.addAttribute("danhSachSapHet", inventoryService.getSapHetHang());

        // Thống kê doanh thu & Mảng vẽ biểu đồ của Bố Duy
        Long totalRevenue = orderRepository.calculateTotalRevenue();
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 1884000L);

        Long[] monthlyRevenue = {1200000L, 1884000L, 1500000L, 2200000L, 3100000L, 2600000L, 0L, 0L, 0L, 0L, 0L, 0L};
        model.addAttribute("monthlyRevenue", monthlyRevenue);

        return "admin/dashboard";   
    }

    // QUẢN LÝ KHO
    @GetMapping("/inventory")
    public String inventory(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";
        
        model.addAttribute("currentPage", "inventory");
        model.addAttribute("danhSachTruyen", inventoryService.getAllMangasForAdmin());
        return "admin/inventory";  
    }

    @PostMapping("/inventory/nhap")
    public String nhapHang(@RequestParam Long mangaId, @RequestParam int soLuong, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/";
        try {
            inventoryService.nhapHang(mangaId, soLuong);
            redirectAttributes.addFlashAttribute("successMsg", "✅ Nhập hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "❌ " + e.getMessage());
        }
        return "redirect:/admin/inventory";
    }

    @PostMapping("/inventory/xuat")
    public String xuatHang(@RequestParam Long mangaId, @RequestParam int soLuong, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/";
        try {
            inventoryService.xuatHang(mangaId, soLuong);
            redirectAttributes.addFlashAttribute("successMsg", "✅ Điều chỉnh thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "❌ " + e.getMessage());
        }
        return "redirect:/admin/inventory";
    }
}