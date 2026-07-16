package com.orderlink.product.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.orderlink.product.domain.Product;
import com.orderlink.product.domain.ProductStatus;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class ProductRepositoryTests {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesProductAndVariantsAsAggregate() {
        Product product = Product.create("Ethiopia Guji", "Natural process coffee beans");
        product.addVariant("GUJI-200G-BEAN", "200g whole bean", new BigDecimal("18000"));
        product.activate();

        Product saved = productRepository.saveAndFlush(product);
        entityManager.clear();

        Product found = productRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(found.getVariants()).hasSize(1);
        assertThat(found.getVariants().getFirst().getSkuCode()).isEqualTo("GUJI-200G-BEAN");
        assertThat(productVariantRepository.existsBySkuCode("GUJI-200G-BEAN")).isTrue();
    }

    @Test
    void enforcesGlobalSkuUniqueness() {
        Product first = Product.create("First Coffee", null);
        first.addVariant("SHARED-SKU", "200g", new BigDecimal("10000"));
        productRepository.saveAndFlush(first);

        Product second = Product.create("Second Coffee", null);
        second.addVariant("SHARED-SKU", "500g", new BigDecimal("20000"));

        assertThatThrownBy(() -> productRepository.saveAndFlush(second))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}
