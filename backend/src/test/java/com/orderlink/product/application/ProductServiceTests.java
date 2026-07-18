package com.orderlink.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.orderlink.grouppurchase.domain.GroupPurchase;
import com.orderlink.grouppurchase.repository.GroupPurchaseRepository;
import com.orderlink.product.domain.Product;
import com.orderlink.product.domain.ProductStatus;
import com.orderlink.product.repository.ProductRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class ProductServiceTests {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private GroupPurchaseRepository groupPurchaseRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void createsProductWithVariants() {
        ProductCreateCommand command = new ProductCreateCommand(
            " Ethiopia Guji ",
            " Natural process coffee beans ",
            List.of(new ProductCreateCommand.Variant(
                " guji-200g-bean ",
                "200g whole bean",
                new BigDecimal("18000")
            ))
        );

        Long productId = productService.create(command);
        entityManager.clear();

        Product product = productRepository.findById(productId).orElseThrow();
        assertThat(product.getName()).isEqualTo("Ethiopia Guji");
        assertThat(product.getDescription()).isEqualTo("Natural process coffee beans");
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(product.getVariants()).hasSize(1);
        assertThat(product.getVariants().getFirst().getSkuCode()).isEqualTo("GUJI-200G-BEAN");
        assertThat(product.getVariants().getFirst().getPrice()).isEqualByComparingTo("18000.00");
    }

    @Test
    void rejectsSkuAlreadyUsedByAnotherProduct() {
        Product existing = Product.create("Existing Coffee", null);
        existing.addVariant("SHARED-SKU", "200g", new BigDecimal("10000"));
        productRepository.saveAndFlush(existing);

        ProductCreateCommand command = new ProductCreateCommand(
            "New Coffee",
            null,
            List.of(new ProductCreateCommand.Variant(
                " shared-sku ",
                "500g",
                new BigDecimal("20000")
            ))
        );

        assertThatThrownBy(() -> productService.create(command))
            .isInstanceOf(DuplicateSkuException.class)
            .hasMessageContaining("SHARED-SKU");
    }

    @Test
    void requiresAtLeastOneVariant() {
        ProductCreateCommand command = new ProductCreateCommand(
            "Coffee Without Variant",
            null,
            List.of()
        );

        assertThatThrownBy(() -> productService.create(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("At least one product variant");
    }

    @Test
    void returnsProductDetail() {
        Product product = Product.create("Kenya Nyeri", "Washed process coffee beans");
        product.addVariant("NYERI-200G", "200g whole bean", new BigDecimal("19000"));
        Long productId = productRepository.saveAndFlush(product).getId();
        entityManager.clear();

        ProductDetailResult result = productService.getDetail(productId);

        assertThat(result.id()).isEqualTo(productId);
        assertThat(result.name()).isEqualTo("Kenya Nyeri");
        assertThat(result.description()).isEqualTo("Washed process coffee beans");
        assertThat(result.status()).isEqualTo(ProductStatus.DRAFT);
        assertThat(result.variants()).hasSize(1);
        assertThat(result.variants().getFirst().id()).isNotNull();
        assertThat(result.variants().getFirst().skuCode()).isEqualTo("NYERI-200G");
        assertThat(result.variants().getFirst().price()).isEqualByComparingTo("19000.00");
        assertThat(result.createdAt()).isNotNull();
        assertThat(result.updatedAt()).isNotNull();
    }

    @Test
    void throwsExceptionWhenProductDoesNotExist() {
        assertThatThrownBy(() -> productService.getDetail(999L))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessage("Product not found: 999");
    }

    @Test
    void returnsPagedProducts() {
        Product first = Product.create("Ethiopia Guji", null);
        first.addVariant("LIST-GUJI-200G", "200g", new BigDecimal("18000"));
        Product second = Product.create("Kenya Nyeri", null);
        second.addVariant("LIST-NYERI-200G", "200g", new BigDecimal("19000"));
        productRepository.saveAllAndFlush(List.of(first, second));

        ProductListResult result = productService.getList(null, PageRequest.of(0, 20));

        assertThat(result.items()).hasSize(2);
        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.page()).isZero();
    }

    @Test
    void returnsProductsByStatus() {
        Product draft = Product.create("Draft Coffee", null);
        draft.addVariant("LIST-DRAFT-200G", "200g", new BigDecimal("18000"));
        Product active = Product.create("Active Coffee", null);
        active.addVariant("LIST-ACTIVE-200G", "200g", new BigDecimal("19000"));
        active.activate();
        productRepository.saveAllAndFlush(List.of(draft, active));

        ProductListResult result = productService.getList(
            ProductStatus.ACTIVE,
            PageRequest.of(0, 20)
        );

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().name()).isEqualTo("Active Coffee");
        assertThat(result.items().getFirst().status()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    void updatesProductInfo() {
        Product product = Product.create("Before Update", "Old description");
        product.addVariant("UPDATE-200G", "200g", new BigDecimal("18000"));
        Long productId = productRepository.saveAndFlush(product).getId();

        productService.update(productId, new ProductUpdateCommand(
            "After Update",
            "New description"
        ));
        entityManager.flush();
        entityManager.clear();

        Product updated = productRepository.findById(productId).orElseThrow();
        assertThat(updated.getName()).isEqualTo("After Update");
        assertThat(updated.getDescription()).isEqualTo("New description");
        assertThat(updated.getVariants()).hasSize(1);
    }

    @Test
    void throwsExceptionWhenUpdatingMissingProduct() {
        ProductUpdateCommand command = new ProductUpdateCommand("Coffee Beans", null);

        assertThatThrownBy(() -> productService.update(999L, command))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessage("Product not found: 999");
    }

    @Test
    void activatesProduct() {
        Product product = Product.create("Ethiopia Guji", null);
        product.addVariant("GUJI-200G", "200g", new BigDecimal("18000"));
        Long productId = productRepository.saveAndFlush(product).getId();

        productService.activate(productId);

        Product activated = productRepository.findById(productId).orElseThrow();
        assertThat(activated.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    void deactivatesProduct() {
        Product product = Product.create("Ethiopia Guji", null);
        product.addVariant("GUJI-200G", "200g", new BigDecimal("18000"));
        product.activate();
        Long productId = productRepository.saveAndFlush(product).getId();

        productService.deactivate(productId);

        Product deactivated = productRepository.findById(productId).orElseThrow();
        assertThat(deactivated.getStatus()).isEqualTo(ProductStatus.INACTIVE);
    }

    @Test
    void throwsExceptionWhenActivatingMissingProduct() {
        assertThatThrownBy(() -> productService.activate(999L))
            .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void deletesDraftProduct() {
        Product product = Product.create("Draft Coffee", null);
        product.addVariant("DELETE-DRAFT-200G", "200g", new BigDecimal("18000"));
        Long productId = productRepository.saveAndFlush(product).getId();

        productService.delete(productId);
        entityManager.flush();

        assertThat(productRepository.existsById(productId)).isFalse();
    }

    @Test
    void rejectsDeletingActiveProduct() {
        Product product = Product.create("Active Coffee", null);
        product.addVariant("DELETE-ACTIVE-200G", "200g", new BigDecimal("18000"));
        product.activate();
        Long productId = productRepository.saveAndFlush(product).getId();

        assertThatThrownBy(() -> productService.delete(productId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Only a draft product can be deleted");
    }

    @Test
    void rejectsDeletingProductWithGroupPurchase() {
        Product product = Product.create("Group Purchase Coffee", null);
        var variant = product.addVariant("DELETE-GROUP-200G", "200g", new BigDecimal("18000"));
        Long productId = productRepository.saveAndFlush(product).getId();
        GroupPurchase groupPurchase = GroupPurchase.create(
            variant,
            "Coffee group purchase",
            new BigDecimal("15000"),
            100,
            Instant.parse("2026-07-20T00:00:00Z"),
            Instant.parse("2026-07-27T00:00:00Z")
        );
        groupPurchaseRepository.saveAndFlush(groupPurchase);

        assertThatThrownBy(() -> productService.delete(productId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Product with group purchases cannot be deleted");
    }

    @Test
    void throwsExceptionWhenDeletingMissingProduct() {
        assertThatThrownBy(() -> productService.delete(999L))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessage("Product not found: 999");
    }
}
