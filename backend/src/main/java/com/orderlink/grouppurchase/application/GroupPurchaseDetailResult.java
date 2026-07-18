package com.orderlink.grouppurchase.application;

import java.math.BigDecimal;
import java.time.Instant;

import com.orderlink.grouppurchase.domain.GroupPurchase;
import com.orderlink.grouppurchase.domain.GroupPurchaseStatus;
import com.orderlink.product.domain.Product;
import com.orderlink.product.domain.ProductVariant;

public record GroupPurchaseDetailResult(
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

    public static GroupPurchaseDetailResult from(GroupPurchase groupPurchase) {
        ProductVariant variant = groupPurchase.getProductVariant();
        Product product = variant.getProduct();

        return new GroupPurchaseDetailResult(
            groupPurchase.getId(),
            groupPurchase.getTitle(),
            groupPurchase.getStatus(),
            groupPurchase.getGroupPrice(),
            groupPurchase.getTargetQuantity(),
            groupPurchase.getStartsAt(),
            groupPurchase.getEndsAt(),
            product.getId(),
            product.getName(),
            variant.getId(),
            variant.getSkuCode(),
            variant.getName(),
            variant.getPrice(),
            groupPurchase.getCreatedAt(),
            groupPurchase.getUpdatedAt()
        );
    }
}
