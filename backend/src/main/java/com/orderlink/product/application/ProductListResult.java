package com.orderlink.product.application;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;

import com.orderlink.product.domain.Product;
import com.orderlink.product.domain.ProductStatus;

public record ProductListResult(
    List<Item> items,
    int page,
    int size,
    long totalElements,
    int totalPages
) {

    public static ProductListResult from(Page<Product> products) {
        List<Item> items = products.getContent().stream()
            .map(Item::from)
            .toList();

        return new ProductListResult(
            items,
            products.getNumber(),
            products.getSize(),
            products.getTotalElements(),
            products.getTotalPages()
        );
    }

    public record Item(
        Long id,
        String name,
        String description,
        ProductStatus status,
        Instant createdAt,
        Instant updatedAt
    ) {

        private static Item from(Product product) {
            return new Item(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
            );
        }
    }
}
