package com.mangaone.controller;

import com.mangaone.entity.Order;
import com.mangaone.entity.User;
import com.mangaone.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ===== KIỂM TRA QUYỀN ADMIN =====
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    // ===== 1. XEM DANH SÁCH ĐƠN HÀNG VÀ LỌC =====
    @GetMapping
    public String listOrders(@RequestParam(value = "status", required = false) String status,
                             @RequestParam(value = "search", required = false) String search,
                             HttpSession session,
                             Model model) {
        if (!isAdmin(session)) {
            return "redirect:/";
        }

        List<Order> orders;

        // Nếu có lọc theo trạng thái
        if (status != null && !status.isEmpty() && !status.equals("ALL")) {
            if (search != null && !search.isEmpty()) {
                // Lọc theo trạng thái và tìm kiếm số điện thoại
                orders = orderService.filterOrdersByStatusAndPhone(status, search);
            } else {
                // Chỉ lọc theo trạng thái
                orders = orderService.getOrdersByStatus(status);
            }
        } else {
            // Nếu có tìm kiếm nhưng không lọc trạng thái
            if (search != null && !search.isEmpty()) {
                // Tìm kiếm theo số điện thoại (ưu tiên) hoặc tên
                orders = orderService.searchOrderByPhone(search);
                if (orders.isEmpty()) {
                    orders = orderService.searchOrderByReceiverName(search);
                }
            } else {
                // Lấy tất cả đơn hàng
                orders = orderService.getAllOrders();
            }
        }

        model.addAttribute("orders", orders);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("searchKeyword", search);
        model.addAttribute("currentUser", session.getAttribute("loggedInUser"));

        return "admin/orders-list";
    }

    // ===== 2. XEM CHI TIẾT ĐƠN HÀNG =====
    @GetMapping("/{orderId}")
    public String viewOrderDetail(@PathVariable Long orderId,
                                  HttpSession session,
                                  Model model) {
        if (!isAdmin(session)) {
            return "redirect:/";
        }

        Optional<Order> optionalOrder = orderService.getOrderById(orderId);
        if (optionalOrder.isEmpty()) {
            return "redirect:/admin/orders";
        }

        Order order = optionalOrder.get();
        model.addAttribute("order", order);
        model.addAttribute("currentUser", session.getAttribute("loggedInUser"));

        return "admin/order-detail";
    }

    // ===== 3. CẬP NHẬT TRẠNG THÁI ĐƠN HÀNG (QUAN TRỌNG) =====
    @PostMapping("/{orderId}/update-status")
    public String updateOrderStatus(@PathVariable Long orderId,
                                    @RequestParam String newStatus,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/";
        }

        try {
            orderService.updateOrderStatus(orderId, newStatus);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Cập nhật trạng thái đơn hàng thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/admin/orders/" + orderId;
    }

    // ===== 4. HỦY ĐƠN HÀNG VÀ HỒI PHỤC KHO =====
    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/";
        }

        try {
            orderService.cancelOrder(orderId);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Hủy đơn hàng thành công! Kho hàng đã được cập nhật.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/admin/orders";
    }
}