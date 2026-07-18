package com.orderlink.product.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class ProductTests {

    @Test
    void createsProductInDraftStatus() {
        Product product = Product.create(" Coffee Beans ", " Freshly roasted ");

        assertThat(product.getName()).isEqualTo("Coffee Beans");
        assertThat(product.getDescription()).isEqualTo("Freshly roasted");
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(product.getVariants()).isEmpty();
    }

    @Test
    void addsVariantAndNormalizesSkuAndPrice() {
        Product product = Product.create("Coffee Beans", null);

        ProductVariant variant = product.addVariant(
            " ethiopia-guji-200g ",
            "Ethiopia Guji 200g",
            new BigDecimal("18000")
        );

        assertThat(product.getVariants()).containsExactly(variant);
        assertThat(variant.getProduct()).isSameAs(product);
        assertThat(variant.getSkuCode()).isEqualTo("ETHIOPIA-GUJI-200G");
        assertThat(variant.getPrice()).isEqualByComparingTo("18000.00");
    }

    @Test
    void rejectsDuplicateSkuWithinProduct() {
        Product product = Product.create("Coffee Beans", null);
        product.addVariant("COFFEE-200G", "200g", new BigDecimal("15000"));

        assertThatIllegalArgumentException()
            .isThrownBy(() -> product.addVariant(" coffee-200g ", "Another 200g", new BigDecimal("16000")))
            .withMessageContaining("unique");
    }

    @Test
    void rejectsInvalidPrice() {
        Product product = Product.create("Coffee Beans", null);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> product.addVariant("COFFEE-200G", "200g", new BigDecimal("-1")))
            .withMessageContaining("negative");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> product.addVariant("COFFEE-500G", "500g", new BigDecimal("100.001")))
            .withMessageContaining("decimal places");
    }

    @Test
    void requiresVariantBeforeActivation() {
        Product product = Product.create("Coffee Beans", null);

        assertThatIllegalStateException()
            .isThrownBy(product::activate)
            .withMessageContaining("variant");

        product.addVariant("COFFEE-200G", "200g", new BigDecimal("15000"));
        product.activate();

        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    void updatesProductInfo() {
        Product product = Product.create("Coffee Beans", "Freshly roasted");

        product.updateInfo(" Ethiopia Guji ", " Natural process ");

        assertThat(product.getName()).isEqualTo("Ethiopia Guji");
        assertThat(product.getDescription()).isEqualTo("Natural process");
    }

    @Test
    void rejectsInvalidProductNameOnUpdate() {
        Product product = Product.create("Coffee Beans", null);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> product.updateInfo(" ", "Description"))
            .withMessageContaining("required");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> product.updateInfo("a".repeat(101), "Description"))
            .withMessageContaining("100 characters");
    }
}
