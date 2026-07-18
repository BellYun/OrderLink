package com.orderlink.grouppurchase.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.orderlink.grouppurchase.domain.GroupPurchase;
import com.orderlink.grouppurchase.domain.GroupPurchaseStatus;
import com.orderlink.product.domain.Product;
import com.orderlink.product.domain.ProductVariant;
import com.orderlink.product.repository.ProductRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class GroupPurchaseRepositoryTests {

    @Autowired
    private GroupPurchaseRepository groupPurchaseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndFindsGroupPurchase() {
        Product product = Product.create("Ethiopia Guji", "Natural process coffee beans");
        ProductVariant variant = product.addVariant("GUJI-200G", "200g", new BigDecimal("20000"));
        product.activate();
        productRepository.saveAndFlush(product);

        GroupPurchase groupPurchase = GroupPurchase.create(
            variant,
            "Ethiopia Guji group purchase",
            new BigDecimal("15000"),
            100,
            Instant.parse("2026-07-20T00:00:00Z"),
            Instant.parse("2026-07-27T00:00:00Z")
        );
        Long groupPurchaseId = groupPurchaseRepository.saveAndFlush(groupPurchase).getId();
        entityManager.clear();

        GroupPurchase found = groupPurchaseRepository.findById(groupPurchaseId).orElseThrow();

        assertThat(found.getProductVariant().getId()).isEqualTo(variant.getId());
        assertThat(found.getProductVariant().getSkuCode()).isEqualTo("GUJI-200G");
        assertThat(found.getTitle()).isEqualTo("Ethiopia Guji group purchase");
        assertThat(found.getGroupPrice()).isEqualByComparingTo("15000.00");
        assertThat(found.getTargetQuantity()).isEqualTo(100);
        assertThat(found.getStatus()).isEqualTo(GroupPurchaseStatus.DRAFT);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }
}
