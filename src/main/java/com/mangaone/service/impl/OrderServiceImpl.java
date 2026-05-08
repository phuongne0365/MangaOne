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

import java.util.List;

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
                orderDetailRepository.save(detail); // Lưu hóa đơn chi tiết
            }
        }

        // PHẦN 2: KHẤU TRỪ KHO & XÓA GIỎ HÀNG
        User user = order.getUser();
        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        // Duyệt qua từng cuốn truyện trong giỏ để trừ kho
        for (CartItem item : cartItems) {
            Manga manga = item.getManga();
            int soLuongMua = item.getQuantity();

            // Kiểm tra an toàn: Nếu lỡ khách mua nhiều hơn số lượng trong kho
            if (manga.getStockQuantity() < soLuongMua) {
                throw new IllegalStateException(
                    "Rất tiếc! Truyện \"" + manga.getTitle() + "\" không đủ hàng. " +
                    "Tồn kho chỉ còn: " + manga.getStockQuantity() + " cuốn.");
            }

            // Trừ đi số lượng đã bán và lưu lại vào kho
            manga.setStockQuantity(manga.getStockQuantity() - soLuongMua);
            mangaRepository.save(manga);
        }

        // Dọn dẹp: Xóa sạch giỏ hàng của khách sau khi đã chốt đơn thành công
        cartItemRepository.deleteByUser(user);

        return savedOrder;
    }
}