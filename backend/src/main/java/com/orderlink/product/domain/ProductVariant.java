package com.orderlink.product.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "product_variants",
    uniqueConstraints = @UniqueConstraint(name = "uk_product_variants_sku_code", columnNames = "sku_code")
)
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sku_code", nullable = false, length = 64)
    private String skuCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProductVariant() {
    }

    private ProductVariant(Product product, String skuCode, String name, BigDecimal price) {
        this.product = product;
        this.skuCode = normalizeSkuCode(skuCode);
        this.name = requireText(name, "Variant name is required");
        this.price = normalizePrice(price);
    }

    static ProductVariant create(Product product, String skuCode, String name, BigDecimal price) {
        if (product == null) {
            throw new IllegalArgumentException("Product is required");
        }
        return new ProductVariant(product, skuCode, name, price);
    }

    static String normalizeSkuCode(String skuCode) {
        String normalized = requireText(skuCode, "SKU code is required").toUpperCase(Locale.ROOT);
        if (normalized.length() > 64) {
            throw new IllegalArgumentException("SKU code must not exceed 64 characters");
        }
        return normalized;
    }

    public void changePrice(BigDecimal price) {
        this.price = normalizePrice(price);
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static BigDecimal normalizePrice(BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException("Price is required");
        }
        if (price.signum() < 0) {
            throw new IllegalArgumentException("Price must not be negative");
        }

        try {
            return price.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Price must have at most two decimal places", exception);
        }
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
