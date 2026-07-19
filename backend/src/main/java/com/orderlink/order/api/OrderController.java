package com.orderlink.order.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orderlink.order.application.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderCreateResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        Long orderId = orderService.create(request.toCommand());
        URI location = URI.create("/api/v1/orders/" + orderId);

        return ResponseEntity.created(location)
            .body(new OrderCreateResponse(orderId));
    }
}
