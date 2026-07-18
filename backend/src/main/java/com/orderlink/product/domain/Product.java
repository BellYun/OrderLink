package com.orderlink.product.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private final List<ProductVariant> variants = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Product() {
    }

    private Product(String name, String description) {
        this.name = normalizeName(name);
        this.description = normalizeDescription(description);
        this.status = ProductStatus.DRAFT;
    }

    public static Product create(String name, String description) {
        return new Product(name, description);
    }

    public ProductVariant addVariant(String skuCode, String name, BigDecimal price) {
        String normalizedSkuCode = ProductVariant.normalizeSkuCode(skuCode);
        boolean duplicated = variants.stream()
            .anyMatch(variant -> variant.getSkuCode().equals(normalizedSkuCode));

        if (duplicated) {
            throw new IllegalArgumentException("SKU code must be unique within a product");
        }

        ProductVariant variant = ProductVariant.create(this, normalizedSkuCode, name, price);
        variants.add(variant);
        return variant;
    }

    public void updateInfo(String name, String description) {
        this.name = normalizeName(name);
        this.description = normalizeDescription(description);
    }

    public void activate() {
        if (variants.isEmpty()) {
            throw new IllegalStateException("A product must have at least one variant before activation");
        }
        status = ProductStatus.ACTIVE;
    }

    public void deactivate() {
        status = ProductStatus.INACTIVE;
    }

    public void validateDeletion() {
        if (status != ProductStatus.DRAFT) {
            throw new IllegalStateException("Only a draft product can be deleted");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public List<ProductVariant> getVariants() {
        return List.copyOf(variants);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private static String normalizeName(String name) {
        String normalized = requireText(name, "Product name is required");
        if (normalized.length() > 100) {
            throw new IllegalArgumentException("Product name must not exceed 100 characters");
        }
        return normalized;
    }

    private static String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }
}
