package com.orderlink.order.domain;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.orderlink.grouppurchase.domain.GroupPurchase;

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
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_purchase_id", nullable = false)
    private GroupPurchase groupPurchase;

    @Column(name = "buyer_id", nullable = false, length = 100)
    private String buyerId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Order() {
    }

    private Order(GroupPurchase groupPurchase, String buyerId, int quantity, Instant orderedAt) {
        this.groupPurchase = requireGroupPurchase(groupPurchase);
        this.groupPurchase.validateOrderable(orderedAt);
        this.buyerId = normalizeBuyerId(buyerId);
        this.quantity = requirePositiveQuantity(quantity);
        this.unitPrice = groupPurchase.getGroupPrice();
        this.totalAmount = unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        this.status = OrderStatus.PLACED;
    }

    public static Order create(
        GroupPurchase groupPurchase,
        String buyerId,
        int quantity,
        Instant orderedAt
    ) {
        return new Order(groupPurchase, buyerId, quantity, orderedAt);
    }

    public Long getId() {
        return id;
    }

    public GroupPurchase getGroupPurchase() {
        return groupPurchase;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static GroupPurchase requireGroupPurchase(GroupPurchase groupPurchase) {
        if (groupPurchase == null) {
            throw new IllegalArgumentException("Group purchase is required");
        }
        return groupPurchase;
    }

    private static String normalizeBuyerId(String buyerId) {
        if (buyerId == null || buyerId.isBlank()) {
            throw new IllegalArgumentException("Buyer ID is required");
        }

        String normalized = buyerId.trim();
        if (normalized.length() > 100) {
            throw new IllegalArgumentException("Buyer ID must not exceed 100 characters");
        }
        return normalized;
    }

    private static int requirePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Order quantity must be positive");
        }
        return quantity;
    }
}
