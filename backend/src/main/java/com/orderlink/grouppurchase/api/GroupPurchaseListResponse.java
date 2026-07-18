package com.orderlink.grouppurchase.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.orderlink.grouppurchase.application.GroupPurchaseListResult;
import com.orderlink.grouppurchase.domain.GroupPurchaseStatus;

public record GroupPurchaseListResponse(
    List<Item> items,
    int page,
    int size,
    long totalElements,
    int totalPages
) {

    public static GroupPurchaseListResponse from(GroupPurchaseListResult result) {
        List<Item> items = result.items().stream()
            .map(Item::from)
            .toList();

        return new GroupPurchaseListResponse(
            items,
            result.page(),
            result.size(),
            result.totalElements(),
            result.totalPages()
        );
    }

    public record Item(
        Long id,
        Long productVariantId,
        String title,
        GroupPurchaseStatus status,
        BigDecimal groupPrice,
        int targetQuantity,
        Instant startsAt,
        Instant endsAt,
        Instant createdAt
    ) {

        private static Item from(GroupPurchaseListResult.Item item) {
            return new Item(
                item.id(),
                item.productVariantId(),
                item.title(),
                item.status(),
                item.groupPrice(),
                item.targetQuantity(),
                item.startsAt(),
                item.endsAt(),
                item.createdAt()
            );
        }
    }
}
