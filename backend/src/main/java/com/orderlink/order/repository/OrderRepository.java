package com.orderlink.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orderlink.order.domain.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
