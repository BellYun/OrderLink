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
}
