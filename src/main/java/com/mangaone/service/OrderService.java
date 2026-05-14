package com.mangaone.service;

import com.mangaone.entity.Order;
import com.mangaone.entity.User;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    Order createOrder(Order order);

    // Phương thức checkout tất cả sản phẩm trong giỏ
    Order checkout(User user, String receiverName, String receiverPhone, String shippingAddress);

    // Phương thức checkout chỉ những sản phẩm được chọn
    Order checkoutSelected(User user, String receiverName, String receiverPhone, String shippingAddress, List<Integer> cartIds);
    // Lấy tất cả đơn hàng
    List<Order> getAllOrders();

    // Lấy chi tiết 1 đơn hàng
    Optional<Order> getOrderById(Long orderId);

    // Lọc theo trạng thái
    List<Order> getOrdersByStatus(String status);

    // Tìm kiếm theo số điện thoại
    List<Order> searchOrderByPhone(String phone);

    // Tìm kiếm theo tên người nhận
    List<Order> searchOrderByReceiverName(String name);

    // Lọc theo trạng thái và số điện thoại
    List<Order> filterOrdersByStatusAndPhone(String status, String phone);

    // Cập nhật trạng thái đơn hàng
    Order updateOrderStatus(Long orderId, String newStatus);

    // Hủy đơn hàng (hồi phục kho hàng)
    Order cancelOrder(Long orderId);
}