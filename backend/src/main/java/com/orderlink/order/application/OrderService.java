package com.orderlink.order.application;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orderlink.grouppurchase.application.GroupPurchaseNotFoundException;
import com.orderlink.grouppurchase.domain.GroupPurchase;
import com.orderlink.grouppurchase.repository.GroupPurchaseRepository;
import com.orderlink.order.domain.Order;
import com.orderlink.order.repository.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final GroupPurchaseRepository groupPurchaseRepository;

    public OrderService(
        OrderRepository orderRepository,
        GroupPurchaseRepository groupPurchaseRepository
    ) {
        this.orderRepository = orderRepository;
        this.groupPurchaseRepository = groupPurchaseRepository;
    }

    @Transactional
    public Long create(OrderCreateCommand command) {
        GroupPurchase groupPurchase = groupPurchaseRepository.findById(command.groupPurchaseId())
            .orElseThrow(() -> new GroupPurchaseNotFoundException(command.groupPurchaseId()));

        Order order = Order.create(
            groupPurchase,
            command.buyerId(),
            command.quantity(),
            Instant.now()
        );

        return orderRepository.saveAndFlush(order).getId();
    }
}
