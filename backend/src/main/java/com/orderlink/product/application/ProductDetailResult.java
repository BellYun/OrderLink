package com.orderlink.product.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.orderlink.product.domain.Product;
import com.orderlink.product.domain.ProductStatus;

public record ProductDetailResult(
    Long id,
    String name,
    String description,
    ProductStatus status,
    List<Variant> variants,
    Instant createdAt,
    Instant updatedAt
) {

    public ProductDetailResult {
        variants = List.copyOf(variants);
    }

    public static ProductDetailResult from(Product product) {
        List<Variant> variants = product.getVariants().stream()
            .map(variant -> new Variant(
                variant.getId(),
                variant.getSkuCode(),
                variant.getName(),
                variant.getPrice()
            ))
            .toList();

        return new ProductDetailResult(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getStatus(),
            variants,
            product.getCreatedAt(),
            product.getUpdatedAt()
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
