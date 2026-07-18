package com.orderlink.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

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
}
