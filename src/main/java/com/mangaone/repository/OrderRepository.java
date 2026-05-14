package com.mangaone.repository;

import com.mangaone.entity.Order;
import com.mangaone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Lấy tất cả đơn hàng, sắp xếp mới nhất trước
    List<Order> findAllByOrderByCreatedAtDesc();

    // Lọc theo trạng thái
    List<Order> findByStatusOrderByCreatedAtDesc(String status);

    // Tìm kiếm theo số điện thoại người nhận
    List<Order> findByReceiverPhoneContainingOrderByCreatedAtDesc(String receiverPhone);

    // Tìm kiếm theo tên người nhận
    List<Order> findByReceiverNameContainingOrderByCreatedAtDesc(String receiverName);

    // Lọc theo trạng thái và số điện thoại
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.receiverPhone LIKE %:phone% ORDER BY o.createdAt DESC")
    List<Order> findByStatusAndPhone(@Param("status") String status, @Param("phone") String phone);

    // Lấy đơn hàng theo user
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    // Lấy chi tiết 1 đơn hàng
    Optional<Order> findById(Long orderId);
}