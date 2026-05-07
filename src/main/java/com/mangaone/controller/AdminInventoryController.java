package com.mangaone.controller;

import com.mangaone.service.InventoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller cho toàn bộ khu vực Admin.
 * Tất cả URL bắt đầu bằng /admin → chỉ Admin mới được truy cập
 * (cấu hình phân quyền trong Spring Security — nếu nhóm có dùng).
 */
@Controller
@RequestMapping("/admin")
public class AdminInventoryController {

    private final InventoryService inventoryService;

    public AdminInventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // THÊM MỚI: /admin → redirect về /admin/dashboard
    @GetMapping
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }
    // ===================================================================
    // TRANG DASHBOARD CHÍNH: GET /admin/dashboard
    // ===================================================================
    @GetMapping("/dashboard")
    public String dashboard(Model model) {

    	model.addAttribute("currentPage", "dashboard"); // ← THÊM DÒNG NÀY
        // Thẻ thống kê
        model.addAttribute("tongSoTruyen",  inventoryService.demTongSoTruyen());
        model.addAttribute("soHetHang",     inventoryService.demHetHang());
        model.addAttribute("soSapHetHang",  inventoryService.demSapHetHang());

        // Bảng cảnh báo sắp hết hàng
        model.addAttribute("danhSachSapHet", inventoryService.getSapHetHang());

        return "admin/dashboard";   // → templates/admin/dashboard.html
    }

    // ===================================================================
    // TRANG QUẢN LÝ KHO: GET /admin/inventory
    // ===================================================================
    @GetMapping("/inventory")
    public String inventory(Model model) {
    	model.addAttribute("currentPage", "inventory"); // ← THÊM DÒNG NÀY
        model.addAttribute("danhSachTruyen", inventoryService.getAllMangasForAdmin());
        return "admin/inventory";   // → templates/admin/inventory.html
    }

    // ===================================================================
    // NHẬP HÀNG: POST /admin/inventory/nhap
    // ===================================================================
    @PostMapping("/inventory/nhap")
    public String nhapHang(@RequestParam Long mangaId,
                           @RequestParam int soLuong,
                           RedirectAttributes redirectAttributes) {
        try {
            inventoryService.nhapHang(mangaId, soLuong);
            redirectAttributes.addFlashAttribute("successMsg",
                    "✅ Nhập hàng thành công! Đã cộng thêm " + soLuong + " cuốn.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "❌ " + e.getMessage());
        }
        return "redirect:/admin/inventory";
    }

    // ===================================================================
    // XUẤT HÀNG / ĐIỀU CHỈNH GIẢM: POST /admin/inventory/xuat
    // ===================================================================
    @PostMapping("/inventory/xuat")
    public String xuatHang(@RequestParam Long mangaId,
                           @RequestParam int soLuong,
                           RedirectAttributes redirectAttributes) {
        try {
            inventoryService.xuatHang(mangaId, soLuong);
            redirectAttributes.addFlashAttribute("successMsg",
                    "✅ Điều chỉnh thành công! Đã trừ " + soLuong + " cuốn.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "❌ " + e.getMessage());
        }
        return "redirect:/admin/inventory";
    }
}
