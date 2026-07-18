package com.orderlink.grouppurchase.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.orderlink.grouppurchase.domain.GroupPurchase;
import com.orderlink.grouppurchase.domain.GroupPurchaseStatus;
import com.orderlink.grouppurchase.repository.GroupPurchaseRepository;
import com.orderlink.product.application.ProductVariantNotFoundException;
import com.orderlink.product.domain.Product;
import com.orderlink.product.domain.ProductVariant;
import com.orderlink.product.repository.ProductRepository;

@SpringBootTest
@Transactional
class GroupPurchaseServiceTests {

    @Autowired
    private GroupPurchaseService groupPurchaseService;

    @Autowired
    private GroupPurchaseRepository groupPurchaseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void createsDraftGroupPurchase() {
        Product product = Product.create("Ethiopia Guji", null);
        ProductVariant variant = product.addVariant("GUJI-200G", "200g", new BigDecimal("20000"));
        productRepository.saveAndFlush(product);

        GroupPurchaseCreateCommand command = new GroupPurchaseCreateCommand(
            variant.getId(),
            " Ethiopia Guji group purchase ",
            new BigDecimal("15000"),
            100,
            Instant.parse("2026-07-20T00:00:00Z"),
            Instant.parse("2026-07-27T00:00:00Z")
        );

        Long groupPurchaseId = groupPurchaseService.create(command);

        assertThat(groupPurchaseId).isNotNull();
        assertThat(groupPurchaseRepository.existsById(groupPurchaseId)).isTrue();
    }

    @Test
    void throwsExceptionWhenProductVariantDoesNotExist() {
        GroupPurchaseCreateCommand command = new GroupPurchaseCreateCommand(
            999L,
            "Ethiopia Guji group purchase",
            new BigDecimal("15000"),
            100,
            Instant.parse("2026-07-20T00:00:00Z"),
            Instant.parse("2026-07-27T00:00:00Z")
        );

        assertThatThrownBy(() -> groupPurchaseService.create(command))
            .isInstanceOf(ProductVariantNotFoundException.class)
            .hasMessage("Product variant not found: 999");
    }

    @Test
    void opensGroupPurchase() {
        Long groupPurchaseId = saveOpenableGroupPurchase();

        groupPurchaseService.open(groupPurchaseId);

        GroupPurchase groupPurchase = groupPurchaseRepository.findById(groupPurchaseId).orElseThrow();
        assertThat(groupPurchase.getStatus()).isEqualTo(GroupPurchaseStatus.OPEN);
    }

    @Test
    void closesGroupPurchase() {
        Long groupPurchaseId = saveOpenableGroupPurchase();
        groupPurchaseService.open(groupPurchaseId);

        groupPurchaseService.close(groupPurchaseId);

        GroupPurchase groupPurchase = groupPurchaseRepository.findById(groupPurchaseId).orElseThrow();
        assertThat(groupPurchase.getStatus()).isEqualTo(GroupPurchaseStatus.CLOSED);
    }

    @Test
    void throwsExceptionWhenGroupPurchaseDoesNotExist() {
        assertThatThrownBy(() -> groupPurchaseService.open(999L))
            .isInstanceOf(GroupPurchaseNotFoundException.class);
    }

    private Long saveOpenableGroupPurchase() {
        Product product = Product.create("Ethiopia Guji", null);
        ProductVariant variant = product.addVariant("OPEN-GUJI-200G", "200g", new BigDecimal("20000"));
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
        return groupPurchaseRepository.saveAndFlush(groupPurchase).getId();
    }
}
