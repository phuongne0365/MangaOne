package com.mangaone.service.impl;

import com.mangaone.entity.Order;
import com.mangaone.entity.OrderDetail;
import com.mangaone.repository.OrderDetailRepository;
import com.mangaone.repository.OrderRepository;
import com.mangaone.service.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Override
    public Order createOrder(Order order) {

        Order savedOrder = orderRepository.save(order);

        if (savedOrder.getOrderDetails() != null) {

            for (OrderDetail detail : savedOrder.getOrderDetails()) {

                detail.setOrder(savedOrder);

                orderDetailRepository.save(detail);
            }
        }

        return savedOrder;
    }
}
