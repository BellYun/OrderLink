package com.orderlink.grouppurchase.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.orderlink.product.domain.ProductStatus;
import com.orderlink.product.domain.ProductVariant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "group_purchases")
public class GroupPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "group_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal groupPrice;

    @Column(name = "target_quantity", nullable = false)
    private int targetQuantity;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupPurchaseStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected GroupPurchase() {
    }

    private GroupPurchase(
        ProductVariant productVariant,
        String title,
        BigDecimal groupPrice,
        int targetQuantity,
        Instant startsAt,
        Instant endsAt
    ) {
        this.productVariant = requireProductVariant(productVariant);
        this.title = normalizeTitle(title);
        this.groupPrice = normalizeGroupPrice(groupPrice, productVariant.getPrice());
        this.targetQuantity = requirePositiveTargetQuantity(targetQuantity);
        this.startsAt = requireInstant(startsAt, "Start time is required");
        this.endsAt = requireInstant(endsAt, "End time is required");
        validatePeriod(this.startsAt, this.endsAt);
        this.status = GroupPurchaseStatus.DRAFT;
    }

    public static GroupPurchase create(
        ProductVariant productVariant,
        String title,
        BigDecimal groupPrice,
        int targetQuantity,
        Instant startsAt,
        Instant endsAt
    ) {
        return new GroupPurchase(productVariant, title, groupPrice, targetQuantity, startsAt, endsAt);
    }

    public void updateRecruitmentInfo(
        String title,
        BigDecimal groupPrice,
        int targetQuantity,
        Instant startsAt,
        Instant endsAt
    ) {
        requireStatus(GroupPurchaseStatus.DRAFT, "Only a draft group purchase can be updated");

        String normalizedTitle = normalizeTitle(title);
        BigDecimal normalizedGroupPrice = normalizeGroupPrice(groupPrice, productVariant.getPrice());
        int normalizedTargetQuantity = requirePositiveTargetQuantity(targetQuantity);
        Instant normalizedStartsAt = requireInstant(startsAt, "Start time is required");
        Instant normalizedEndsAt = requireInstant(endsAt, "End time is required");
        validatePeriod(normalizedStartsAt, normalizedEndsAt);

        this.title = normalizedTitle;
        this.groupPrice = normalizedGroupPrice;
        this.targetQuantity = normalizedTargetQuantity;
        this.startsAt = normalizedStartsAt;
        this.endsAt = normalizedEndsAt;
    }

    public void open(Instant openedAt) {
        requireStatus(GroupPurchaseStatus.DRAFT, "Only a draft group purchase can be opened");
        if (productVariant.getProduct().getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalStateException("Product must be active before opening a group purchase");
        }

        Instant normalizedOpenedAt = requireInstant(openedAt, "Open time is required");
        if (normalizedOpenedAt.isBefore(startsAt) || !normalizedOpenedAt.isBefore(endsAt)) {
            throw new IllegalStateException("Group purchase can only be opened during its recruitment period");
        }
        status = GroupPurchaseStatus.OPEN;
    }

    public void close() {
        requireStatus(GroupPurchaseStatus.OPEN, "Only an open group purchase can be closed");
        status = GroupPurchaseStatus.CLOSED;
    }

    public void cancel() {
        if (status != GroupPurchaseStatus.DRAFT && status != GroupPurchaseStatus.OPEN) {
            throw new IllegalStateException("Only a draft or open group purchase can be cancelled");
        }
        status = GroupPurchaseStatus.CANCELLED;
    }

    public Long getId() {
        return id;
    }

    public ProductVariant getProductVariant() {
        return productVariant;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getGroupPrice() {
        return groupPrice;
    }

    public int getTargetQuantity() {
        return targetQuantity;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public GroupPurchaseStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private void requireStatus(GroupPurchaseStatus expected, String message) {
        if (status != expected) {
            throw new IllegalStateException(message);
        }
    }

    private static ProductVariant requireProductVariant(ProductVariant productVariant) {
        if (productVariant == null) {
            throw new IllegalArgumentException("Product variant is required");
        }
        return productVariant;
    }

    private static String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }

        String normalized = title.trim();
        if (normalized.length() > 100) {
            throw new IllegalArgumentException("Title must not exceed 100 characters");
        }
        return normalized;
    }

    private static BigDecimal normalizeGroupPrice(BigDecimal groupPrice, BigDecimal regularPrice) {
        if (groupPrice == null) {
            throw new IllegalArgumentException("Group price is required");
        }
        if (groupPrice.signum() <= 0) {
            throw new IllegalArgumentException("Group price must be positive");
        }

        BigDecimal normalized;
        try {
            normalized = groupPrice.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Group price must have at most two decimal places", exception);
        }

        if (normalized.compareTo(regularPrice) > 0) {
            throw new IllegalArgumentException("Group price must not exceed the regular price");
        }
        return normalized;
    }

    private static int requirePositiveTargetQuantity(int targetQuantity) {
        if (targetQuantity <= 0) {
            throw new IllegalArgumentException("Target quantity must be positive");
        }
        return targetQuantity;
    }

    private static Instant requireInstant(Instant value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static void validatePeriod(Instant startsAt, Instant endsAt) {
        if (!endsAt.isAfter(startsAt)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }
}
