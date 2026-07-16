package com.orderlink.product.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.orderlink.product.application.ProductDetailResult;
import com.orderlink.product.domain.ProductStatus;

public record ProductDetailResponse(
    Long id,
    String name,
    String description,
    ProductStatus status,
    List<Variant> variants,
    Instant createdAt,
    Instant updatedAt
) {

    public ProductDetailResponse {
        variants = List.copyOf(variants);
    }

    public static ProductDetailResponse from(ProductDetailResult result) {
        List<Variant> variants = result.variants().stream()
            .map(variant -> new Variant(
                variant.id(),
                variant.skuCode(),
                variant.name(),
                variant.price()
            ))
            .toList();

        return new ProductDetailResponse(
            result.id(),
            result.name(),
            result.description(),
            result.status(),
            variants,
            result.createdAt(),
            result.updatedAt()
        );
    }

    public record Variant(
        Long id,
        String skuCode,
        String name,
        BigDecimal price
    ) {
    }
}
