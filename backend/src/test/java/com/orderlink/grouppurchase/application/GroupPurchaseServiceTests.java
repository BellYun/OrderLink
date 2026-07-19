package com.orderlink.grouppurchase.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
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
    void updatesGroupPurchaseRecruitmentInfo() {
        Product product = Product.create("Ethiopia Guji", null);
        ProductVariant variant = product.addVariant("UPDATE-GUJI-200G", "200g", new BigDecimal("20000"));
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
        GroupPurchaseUpdateCommand command = new GroupPurchaseUpdateCommand(
            "Updated group purchase",
            new BigDecimal("14000"),
            80,
            Instant.parse("2026-07-21T00:00:00Z"),
            Instant.parse("2026-07-28T00:00:00Z")
        );

        groupPurchaseService.update(groupPurchaseId, command);

        GroupPurchase updated = groupPurchaseRepository.findById(groupPurchaseId).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Updated group purchase");
        assertThat(updated.getGroupPrice()).isEqualByComparingTo("14000.00");
        assertThat(updated.getTargetQuantity()).isEqualTo(80);
    }

    @Test
    void throwsExceptionWhenUpdatingMissingGroupPurchase() {
        GroupPurchaseUpdateCommand command = new GroupPurchaseUpdateCommand(
            "Updated group purchase",
            new BigDecimal("14000"),
            80,
            Instant.parse("2026-07-21T00:00:00Z"),
            Instant.parse("2026-07-28T00:00:00Z")
        );

        assertThatThrownBy(() -> groupPurchaseService.update(999L, command))
            .isInstanceOf(GroupPurchaseNotFoundException.class)
            .hasMessage("Group purchase not found: 999");
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
    void cancelsGroupPurchase() {
        Long groupPurchaseId = saveOpenableGroupPurchase();

        groupPurchaseService.cancel(groupPurchaseId);

        GroupPurchase groupPurchase = groupPurchaseRepository.findById(groupPurchaseId).orElseThrow();
        assertThat(groupPurchase.getStatus()).isEqualTo(GroupPurchaseStatus.CANCELLED);
    }

    @Test
    void throwsExceptionWhenCancellingMissingGroupPurchase() {
        assertThatThrownBy(() -> groupPurchaseService.cancel(999L))
            .isInstanceOf(GroupPurchaseNotFoundException.class)
            .hasMessage("Group purchase not found: 999");
    }

    @Test
    void deletesDraftGroupPurchase() {
        Long groupPurchaseId = saveOpenableGroupPurchase();

        groupPurchaseService.delete(groupPurchaseId);
        groupPurchaseRepository.flush();

        assertThat(groupPurchaseRepository.existsById(groupPurchaseId)).isFalse();
    }

    @Test
    void rejectsDeletingOpenGroupPurchase() {
        Long groupPurchaseId = saveOpenableGroupPurchase();
        groupPurchaseService.open(groupPurchaseId);

        assertThatThrownBy(() -> groupPurchaseService.delete(groupPurchaseId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Only a draft group purchase can be deleted");
    }

    @Test
    void throwsExceptionWhenDeletingMissingGroupPurchase() {
        assertThatThrownBy(() -> groupPurchaseService.delete(999L))
            .isInstanceOf(GroupPurchaseNotFoundException.class)
            .hasMessage("Group purchase not found: 999");
    }

    @Test
    void throwsExceptionWhenGroupPurchaseDoesNotExist() {
        assertThatThrownBy(() -> groupPurchaseService.open(999L))
            .isInstanceOf(GroupPurchaseNotFoundException.class);
    }

    @Test
    void returnsGroupPurchaseDetail() {
        Product product = Product.create("Ethiopia Guji", null);
        ProductVariant variant = product.addVariant("DETAIL-GUJI-200G", "200g", new BigDecimal("20000"));
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

        GroupPurchaseDetailResult result = groupPurchaseService.getDetail(groupPurchaseId);

        assertThat(result.id()).isEqualTo(groupPurchaseId);
        assertThat(result.productName()).isEqualTo("Ethiopia Guji");
        assertThat(result.skuCode()).isEqualTo("DETAIL-GUJI-200G");
        assertThat(result.status()).isEqualTo(GroupPurchaseStatus.DRAFT);
    }

    @Test
    void returnsPagedGroupPurchasesByStatus() {
        Product product = Product.create("Ethiopia Guji", null);
        ProductVariant variant = product.addVariant("LIST-GUJI-200G", "200g", new BigDecimal("20000"));
        product.activate();
        productRepository.saveAndFlush(product);

        Instant now = Instant.now();
        GroupPurchase draft = GroupPurchase.create(
            variant,
            "Draft group purchase",
            new BigDecimal("15000"),
            100,
            now.minusSeconds(60),
            now.plusSeconds(3600)
        );
        GroupPurchase open = GroupPurchase.create(
            variant,
            "Open group purchase",
            new BigDecimal("14000"),
            80,
            now.minusSeconds(60),
            now.plusSeconds(3600)
        );
        open.open(now);
        groupPurchaseRepository.saveAllAndFlush(java.util.List.of(draft, open));

        GroupPurchaseListResult result = groupPurchaseService.getList(
            GroupPurchaseStatus.OPEN,
            PageRequest.of(0, 10)
        );

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().title()).isEqualTo("Open group purchase");
        assertThat(result.items().getFirst().status()).isEqualTo(GroupPurchaseStatus.OPEN);
        assertThat(result.totalElements()).isEqualTo(1);
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
