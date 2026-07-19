package com.orderlink.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.orderlink.grouppurchase.domain.GroupPurchase;
import com.orderlink.product.domain.Product;
import com.orderlink.product.domain.ProductVariant;

class OrderTests {

    private static final Instant ORDERED_AT = Instant.parse("2026-07-20T12:00:00Z");

    @Test
    void createsOrderWithPriceSnapshot() {
        GroupPurchase groupPurchase = createOpenGroupPurchase();

        Order order = Order.create(groupPurchase, " buyer-1 ", 2, ORDERED_AT);

        assertThat(order.getBuyerId()).isEqualTo("buyer-1");
        assertThat(order.getQuantity()).isEqualTo(2);
        assertThat(order.getUnitPrice()).isEqualByComparingTo("15000.00");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("30000.00");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED);
    }

    @Test
    void rejectsOrderForDraftGroupPurchase() {
        GroupPurchase groupPurchase = createDraftGroupPurchase();

        assertThatIllegalStateException()
            .isThrownBy(() -> Order.create(groupPurchase, "buyer-1", 1, ORDERED_AT))
            .withMessage("Only an open group purchase can receive orders");
    }

    @Test
    void rejectsInvalidBuyerAndQuantity() {
        GroupPurchase groupPurchase = createOpenGroupPurchase();

        assertThatIllegalArgumentException()
            .isThrownBy(() -> Order.create(groupPurchase, " ", 1, ORDERED_AT))
            .withMessage("Buyer ID is required");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> Order.create(groupPurchase, "buyer-1", 0, ORDERED_AT))
            .withMessage("Order quantity must be positive");
    }

    private static GroupPurchase createOpenGroupPurchase() {
        GroupPurchase groupPurchase = createDraftGroupPurchase();
        groupPurchase.getProductVariant().getProduct().activate();
        groupPurchase.open(ORDERED_AT);
        return groupPurchase;
    }

    private static GroupPurchase createDraftGroupPurchase() {
        Product product = Product.create("Ethiopia Guji", null);
        ProductVariant variant = product.addVariant("ORDER-GUJI-200G", "200g", new BigDecimal("20000"));

        return GroupPurchase.create(
            variant,
            "Ethiopia Guji group purchase",
            new BigDecimal("15000"),
            100,
            ORDERED_AT.minusSeconds(60),
            ORDERED_AT.plusSeconds(3600)
        );
    }
}
