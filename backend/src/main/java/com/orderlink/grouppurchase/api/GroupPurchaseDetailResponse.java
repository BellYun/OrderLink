package com.orderlink.grouppurchase.api;

import java.math.BigDecimal;
import java.time.Instant;

import com.orderlink.grouppurchase.application.GroupPurchaseDetailResult;
import com.orderlink.grouppurchase.domain.GroupPurchaseStatus;

public record GroupPurchaseDetailResponse(
    Long id,
    String title,
    GroupPurchaseStatus status,
    BigDecimal groupPrice,
    int targetQuantity,
    Instant startsAt,
    Instant endsAt,
    Long productId,
    String productName,
    Long productVariantId,
    String skuCode,
    String variantName,
    BigDecimal regularPrice,
    Instant createdAt,
    Instant updatedAt
) {

    public static GroupPurchaseDetailResponse from(GroupPurchaseDetailResult result) {
        return new GroupPurchaseDetailResponse(
            result.id(),
            result.title(),
            result.status(),
            result.groupPrice(),
            result.targetQuantity(),
            result.startsAt(),
            result.endsAt(),
            result.productId(),
            result.productName(),
            result.productVariantId(),
            result.skuCode(),
            result.variantName(),
            result.regularPrice(),
            result.createdAt(),
            result.updatedAt()
        );
    }
}
