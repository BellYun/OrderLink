package com.orderlink.product.api;

import java.time.Instant;
import java.util.List;

import com.orderlink.product.application.ProductListResult;
import com.orderlink.product.domain.ProductStatus;

public record ProductListResponse(
    List<Item> items,
    int page,
    int size,
    long totalElements,
    int totalPages
) {

    public static ProductListResponse from(ProductListResult result) {
        List<Item> items = result.items().stream()
            .map(Item::from)
            .toList();

        return new ProductListResponse(
            items,
            result.page(),
            result.size(),
            result.totalElements(),
            result.totalPages()
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

        private static Item from(ProductListResult.Item item) {
            return new Item(
                item.id(),
                item.name(),
                item.description(),
                item.status(),
                item.createdAt(),
                item.updatedAt()
            );
        }
    }
}
