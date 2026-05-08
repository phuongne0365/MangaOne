package com.mangaone.controller;

import com.mangaone.service.InventoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Tất cả URL bắt đầu bằng /admin → chỉ Admin mới được truy cập
 */
@Controller
@RequestMapping("/admin")
public class AdminInventoryController {

    private final InventoryService inventoryService;

    public AdminInventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // 
    @GetMapping
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    // TRANG DASHBOARD : GET /admin/dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model) {

    	model.addAttribute("currentPage", "dashboard"); 
        // Thẻ thống kê
        model.addAttribute("tongSoTruyen",  inventoryService.demTongSoTruyen());
        model.addAttribute("soHetHang",     inventoryService.demHetHang());
        model.addAttribute("soSapHetHang",  inventoryService.demSapHetHang());

        // Bảng cảnh báo sắp hết hàng
        model.addAttribute("danhSachSapHet", inventoryService.getSapHetHang());

        return "admin/dashboard";   
    }

    // TRANG QUẢN LÝ KHO: GET /admin/inventory
    @GetMapping("/inventory")
    public String inventory(Model model) {
    	model.addAttribute("currentPage", "inventory");
        model.addAttribute("danhSachTruyen", inventoryService.getAllMangasForAdmin());
        return "admin/inventory";  
    }

    // NHẬP HÀNG: POST /admin/inventory/nhap
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

    // XUẤT HÀNG / ĐIỀU CHỈNH GIẢM: POST /admin/inventory/xuat
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
