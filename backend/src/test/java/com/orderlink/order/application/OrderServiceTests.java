package com.orderlink.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.orderlink.grouppurchase.application.GroupPurchaseNotFoundException;
import com.orderlink.grouppurchase.domain.GroupPurchase;
import com.orderlink.grouppurchase.repository.GroupPurchaseRepository;
import com.orderlink.order.domain.Order;
import com.orderlink.order.repository.OrderRepository;
import com.orderlink.product.domain.Product;
import com.orderlink.product.domain.ProductVariant;
import com.orderlink.product.repository.ProductRepository;

@SpringBootTest
@Transactional
class OrderServiceTests {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private GroupPurchaseRepository groupPurchaseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void createsOrder() {
        Long groupPurchaseId = saveGroupPurchase(true);

        Long orderId = orderService.create(new OrderCreateCommand(groupPurchaseId, "buyer-1", 2));

        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getGroupPurchase().getId()).isEqualTo(groupPurchaseId);
        assertThat(order.getQuantity()).isEqualTo(2);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("30000.00");
    }

    @Test
    void rejectsOrderForDraftGroupPurchase() {
        Long groupPurchaseId = saveGroupPurchase(false);

        assertThatThrownBy(() -> orderService.create(
            new OrderCreateCommand(groupPurchaseId, "buyer-1", 1)
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Only an open group purchase can receive orders");
    }

    @Test
    void throwsExceptionWhenGroupPurchaseDoesNotExist() {
        assertThatThrownBy(() -> orderService.create(new OrderCreateCommand(999L, "buyer-1", 1)))
            .isInstanceOf(GroupPurchaseNotFoundException.class)
            .hasMessage("Group purchase not found: 999");
    }

    private Long saveGroupPurchase(boolean open) {
        Product product = Product.create("Ethiopia Guji", null);
        ProductVariant variant = product.addVariant("ORDER-GUJI-200G", "200g", new BigDecimal("20000"));
        product.activate();
        productRepository.saveAndFlush(product);

        Instant now = Instant.now();
        GroupPurchase groupPurchase = GroupPurchase.create(
            variant,
            "Ethiopia Guji group purchase",
            new BigDecimal("15000"),
            100,
            now.minusSeconds(60),
            now.plusSeconds(3600)
        );
        if (open) {
            groupPurchase.open(now);
        }
        return groupPurchaseRepository.saveAndFlush(groupPurchase).getId();
    }
}
