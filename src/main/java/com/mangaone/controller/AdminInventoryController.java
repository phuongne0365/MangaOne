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
        model.addAttribute("soHetHang",      inventoryService.demHetHang());
        model.addAttribute("soSapHetHang",  inventoryService.demSapHetHang());
        model.addAttribute("danhSachSapHet", inventoryService.getSapHetHang());

        // 💰 1. Thống kê tổng doanh thu thực tế từ database 
        Long totalRevenue = orderRepository.calculateTotalRevenue();
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0L);

        // 📈 2. ĐOẠN ĐÃ ĐỔI: Khớp dữ liệu List<Object[]> của vào mảng 12 tháng
        Long[] monthlyRevenue = new Long[12];
        // Khởi tạo tất cả các tháng bằng 0 tránh bị lỗi NULL biểu đồ
        java.util.Arrays.fill(monthlyRevenue, 0L); 

        // Lấy danh sách doanh thu các tháng từ câu lệnh Query 
        List<Object[]> rawData = orderRepository.getRevenueByMonth();
        
        // Đổ data thực tế vào đúng vị trí tháng trong mảng (Tháng 1 -> Index 0)
        for (Object[] row : rawData) {
            if (row[0] != null && row[1] != null) {
                int month = ((Number) row[0]).intValue(); // Lấy số tháng (1 - 12)
                Long amount = ((Number) row[1]).longValue(); // Lấy tổng tiền của tháng đó
                
                if (month >= 1 && month <= 12) {
                    monthlyRevenue[month - 1] = amount; // Đưa vào mảng (Index từ 0 đến 11)
                }
            }
        }
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