package com.orderlink.grouppurchase.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;

import com.orderlink.grouppurchase.domain.GroupPurchase;
import com.orderlink.grouppurchase.domain.GroupPurchaseStatus;

public record GroupPurchaseListResult(
    List<Item> items,
    int page,
    int size,
    long totalElements,
    int totalPages
) {

    public static GroupPurchaseListResult from(Page<GroupPurchase> groupPurchases) {
        List<Item> items = groupPurchases.getContent().stream()
            .map(Item::from)
            .toList();

        return new GroupPurchaseListResult(
            items,
            groupPurchases.getNumber(),
            groupPurchases.getSize(),
            groupPurchases.getTotalElements(),
            groupPurchases.getTotalPages()
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

        private static Item from(GroupPurchase groupPurchase) {
            return new Item(
                groupPurchase.getId(),
                groupPurchase.getProductVariant().getId(),
                groupPurchase.getTitle(),
                groupPurchase.getStatus(),
                groupPurchase.getGroupPrice(),
                groupPurchase.getTargetQuantity(),
                groupPurchase.getStartsAt(),
                groupPurchase.getEndsAt(),
                groupPurchase.getCreatedAt()
            );
        }
    }
}
