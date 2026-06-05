package com.mangaone.service.impl;

import com.mangaone.entity.CartItem;
import com.mangaone.entity.Manga;
import com.mangaone.entity.Order;
import com.mangaone.entity.OrderDetail;
import com.mangaone.entity.User;
import com.mangaone.repository.CartItemRepository;
import com.mangaone.repository.MangaRepository;
import com.mangaone.repository.OrderDetailRepository;
import com.mangaone.repository.OrderRepository;
import com.mangaone.service.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private MangaRepository mangaRepository;

    @Override
    @Transactional
    public Order createOrder(Order order) {
        // PHẦN 1: LƯU ĐƠN HÀNG
        Order savedOrder = orderRepository.save(order);

        if (savedOrder.getOrderDetails() != null) {
            for (OrderDetail detail : savedOrder.getOrderDetails()) {
                detail.setOrder(savedOrder);
                orderDetailRepository.save(detail);
            }
        }

        // PHẦN 2: KHẤU TRỪ KHO & XÓA GIỎ HÀNG
        User user = order.getUser();
        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        for (CartItem item : cartItems) {
            Manga manga = item.getManga();
            int soLuongMua = item.getQuantity();

            if (manga.getStockQuantity() < soLuongMua) {
                throw new IllegalStateException(
                        "Rất tiếc! Truyện \"" + manga.getTitle() + "\" không đủ hàng. " +
                                "Tồn kho chỉ còn: " + manga.getStockQuantity() + " cuốn.");
            }

            manga.setStockQuantity(manga.getStockQuantity() - soLuongMua);
            mangaRepository.save(manga);
        }

        cartItemRepository.deleteByUser(user);

        return savedOrder;
    }

    @Override
    @Transactional
    public Order checkout(User user, String receiverName, String receiverPhone, String shippingAddress, String paymentMethod) {
        // Bước 1: Validate input
        if (user == null) {
            throw new IllegalStateException("User không tồn tại!");
        }
        if (receiverName == null || receiverName.trim().isEmpty()) {
            throw new IllegalStateException("Tên người nhận không được để trống!");
        }
        if (receiverPhone == null || receiverPhone.trim().isEmpty()) {
            throw new IllegalStateException("Số điện thoại không được để trống!");
        }
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            throw new IllegalStateException("Địa chỉ giao hàng không được để trống!");
        }

        // Bước 2: Lấy danh sách CartItem của user
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống!");
        }

        // Bước 3: Tính tổng tiền
        double totalAmount = 0.0;
        for (CartItem item : cartItems) {
            if (item.getManga() == null) {
                throw new IllegalStateException("Sản phẩm trong giỏ hàng không hợp lệ!");
            }
            Double price = item.getManga().getPrice();
            if (price == null) {
                throw new IllegalStateException("Giá sản phẩm không hợp lệ!");
            }
            Integer quantity = item.getQuantity();
            if (quantity == null || quantity <= 0) {
                throw new IllegalStateException("Số lượng sản phẩm không hợp lệ!");
            }
            totalAmount += price * quantity;
        }

        // Bước 4: Tạo Order mới và set thông tin phương thức thanh toán
        Order order = new Order();
        order.setUser(user);
        order.setReceiverName(receiverName.trim());
        order.setReceiverPhone(receiverPhone.trim());
        order.setShippingAddress(shippingAddress.trim());
        order.setTotalAmount((int) Math.round(totalAmount));
        order.setPaymentMethod(paymentMethod); // 🔥 BỔ SUNG: Lưu phương thức thanh toán vào DB
        order.setCreatedAt(LocalDateTime.now());

        // 💰 BỔ SUNG: Theo yêu cầu mới, tất cả đơn hàng (COD hay BANK_TRANSFER) đều vào trạng thái PENDING chờ admin duyệt
        order.setStatus("PENDING");

        // Bước 5: Lưu Order trước
        Order savedOrder = orderRepository.save(order);

        // Bước 6: Duyệt CartItems, kiểm tra stock, tạo OrderDetail, trừ stock
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartItem item : cartItems) {
            Manga manga = item.getManga();
            int quantity = item.getQuantity();

            String mangaTitle = manga.getTitle();
            Long mangaId = manga.getId();

            // Refresh manga từ DB để đảm bảo dữ liệu tồn kho đồng bộ
            manga = mangaRepository.findById(mangaId)
                    .orElseThrow(() -> new IllegalStateException("Sản phẩm không tồn tại: " + mangaTitle));

            // Kiểm tra stock kho hàng
            if (manga.getStockQuantity() == null || manga.getStockQuantity() < quantity) {
                throw new IllegalStateException("Truyện \"" + manga.getTitle() + "\" không đủ hàng. Tồn kho: " + (manga.getStockQuantity() != null ? manga.getStockQuantity() : 0));
            }

            // Tạo cấu trúc dữ liệu OrderDetail
            OrderDetail detail = new OrderDetail();
            detail.setOrder(savedOrder);
            detail.setManga(manga);
            detail.setQuantity(quantity);
            detail.setPrice((int) Math.round(manga.getPrice()));
            orderDetailRepository.save(detail);
            orderDetails.add(detail);

            // Khấu trừ hàng trong Database
            manga.setStockQuantity(manga.getStockQuantity() - quantity);
            mangaRepository.save(manga);
        }

        // Bước 7: Giải phóng toàn bộ giỏ hàng của User sau khi đặt hàng thành công
        cartItemRepository.deleteByUser(user);

        // Bước 8: Gắn tập hợp chi tiết đơn hàng vào đối tượng Order trả về
        savedOrder.setOrderDetails(orderDetails);

        return savedOrder;
    }

    @Override
    @Transactional
    public Order checkoutSelected(User user, String receiverName, String receiverPhone, String shippingAddress, List<Integer> cartIds, String paymentMethod) {
        // Bước 1: Validate input
        if (user == null) {
            throw new IllegalStateException("User không tồn tại!");
        }
        if (receiverName == null || receiverName.trim().isEmpty()) {
            throw new IllegalStateException("Tên người nhận không được để trống!");
        }
        if (receiverPhone == null || receiverPhone.trim().isEmpty()) {
            throw new IllegalStateException("Số điện thoại không được để trống!");
        }
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            throw new IllegalStateException("Địa chỉ giao hàng không được để trống!");
        }

        // Bước 2: Lấy danh sách các CartItem được tích chọn
        if (cartIds == null || cartIds.isEmpty()) {
            throw new IllegalStateException("Vui lòng chọn ít nhất 1 sản phẩm!");
        }

        List<CartItem> cartItems = cartItemRepository.findAllById(cartIds);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy sản phẩm được chọn!");
        }

        // Bước 3: Tính tổng tiền các sản phẩm được chọn
        double totalAmount = 0.0;
        for (CartItem item : cartItems) {
            if (item.getManga() == null) {
                throw new IllegalStateException("Sản phẩm trong giỏ hàng không hợp lệ!");
            }
            Double price = item.getManga().getPrice();
            if (price == null) {
                throw new IllegalStateException("Giá sản phẩm không hợp lệ!");
            }
            Integer quantity = item.getQuantity();
            if (quantity == null || quantity <= 0) {
                throw new IllegalStateException("Số lượng sản phẩm không hợp lệ!");
            }
            totalAmount += price * quantity;
        }

        // Bước 4: Khởi tạo Order mới cho luồng mua một phần giỏ hàng
        Order order = new Order();
        order.setUser(user);
        order.setReceiverName(receiverName.trim());
        order.setReceiverPhone(receiverPhone.trim());
        order.setShippingAddress(shippingAddress.trim());
        order.setTotalAmount((int) Math.round(totalAmount));
        order.setPaymentMethod(paymentMethod); // 🔥 BỔ SUNG: Lưu cấu hình thanh toán
        order.setCreatedAt(LocalDateTime.now());

        // 💰 BỔ SUNG: Theo yêu cầu mới, tất cả đơn hàng (COD hay BANK_TRANSFER) đều vào trạng thái PENDING chờ admin duyệt
        order.setStatus("PENDING");

        // Bước 5: Lưu thông tin Order
        Order savedOrder = orderRepository.save(order);

        // Bước 6: Xử lý ghi nhận chi tiết hóa đơn và trừ tồn kho truyện
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartItem item : cartItems) {
            Manga manga = item.getManga();
            int quantity = item.getQuantity();

            String mangaTitle = manga.getTitle();
            Long mangaId = manga.getId();

            manga = mangaRepository.findById(mangaId)
                    .orElseThrow(() -> new IllegalStateException("Sản phẩm không tồn tại: " + mangaTitle));

            if (manga.getStockQuantity() == null || manga.getStockQuantity() < quantity) {
                throw new IllegalStateException("Truyện \"" + manga.getTitle() + "\" không đủ hàng. Tồn kho: " + (manga.getStockQuantity() != null ? manga.getStockQuantity() : 0));
            }

            OrderDetail detail = new OrderDetail();
            detail.setOrder(savedOrder);
            detail.setManga(manga);
            detail.setQuantity(quantity);
            detail.setPrice((int) Math.round(manga.getPrice()));
            orderDetailRepository.save(detail);
            orderDetails.add(detail);

            manga.setStockQuantity(manga.getStockQuantity() - quantity);
            mangaRepository.save(manga);
        }

        // Bước 7: Chỉ xóa những CartItems đã được mua ra khỏi giỏ hàng (giữ lại các item không chọn)
        for (Integer cartId : cartIds) {
            cartItemRepository.deleteById(cartId);
        }

        // Bước 8: Đính kết tập hợp chi tiết
        savedOrder.setOrderDetails(orderDetails);

        return savedOrder;
    }

    // ===== ADMIN CÓ THỂ LẤY TẤT CẢ ĐƠN HÀNG =====
    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    public List<Order> searchOrderByPhone(String phone) {
        return orderRepository.findByReceiverPhoneContainingOrderByCreatedAtDesc(phone);
    }

    @Override
    public List<Order> searchOrderByReceiverName(String name) {
        return orderRepository.findByReceiverNameContainingOrderByCreatedAtDesc(name);
    }

    @Override
    public List<Order> filterOrdersByStatusAndPhone(String status, String phone) {
        return orderRepository.findByStatusAndPhone(status, phone);
    }

    // ===== CẬP NHẬT TRẠNG THÁI ĐƠN HÀNG =====
    @Override
    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại!"));

        // Kiểm tra trạng thái hợp lệ
        String[] validStatuses = {"PENDING", "CONFIRMED", "SHIPPING", "COMPLETED", "CANCELLED"};
        boolean isValid = false;
        for (String status : validStatuses) {
            if (status.equals(newStatus)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + newStatus);
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    // ===== HỦY ĐƠN HÀNG VÀ HỒI PHỤC KHO =====
    @Override
    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại!"));

        // Chỉ có thể hủy những đơn ở trạng thái PENDING hoặc CONFIRMED
        if (!order.getStatus().equals("PENDING") && !order.getStatus().equals("CONFIRMED")) {
            throw new IllegalStateException("Chỉ có thể hủy đơn hàng ở trạng thái PENDING hoặc CONFIRMED!");
        }

        // Hồi phục lại stock cho từng sản phẩm
        List<OrderDetail> orderDetails = order.getOrderDetails();
        for (OrderDetail detail : orderDetails) {
            Manga manga = detail.getManga();
            manga.setStockQuantity(manga.getStockQuantity() + detail.getQuantity());
            mangaRepository.save(manga);
        }

        // Đánh dấu đơn hàng là CANCELLED
        order.setStatus("CANCELLED");
        return orderRepository.save(order);
    }
}