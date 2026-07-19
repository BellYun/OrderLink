package com.orderlink.grouppurchase.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.orderlink.product.domain.Product;
import com.orderlink.product.domain.ProductVariant;

class GroupPurchaseTests {

    private static final Instant STARTS_AT = Instant.parse("2026-07-20T00:00:00Z");
    private static final Instant ENDS_AT = Instant.parse("2026-07-27T00:00:00Z");

    @Test
    void createsGroupPurchaseInDraftStatus() {
        ProductVariant variant = createVariant();

        GroupPurchase groupPurchase = GroupPurchase.create(
            variant,
            " Ethiopia Guji group purchase ",
            new BigDecimal("15000"),
            100,
            STARTS_AT,
            ENDS_AT
        );

        assertThat(groupPurchase.getProductVariant()).isSameAs(variant);
        assertThat(groupPurchase.getTitle()).isEqualTo("Ethiopia Guji group purchase");
        assertThat(groupPurchase.getGroupPrice()).isEqualByComparingTo("15000.00");
        assertThat(groupPurchase.getTargetQuantity()).isEqualTo(100);
        assertThat(groupPurchase.getStartsAt()).isEqualTo(STARTS_AT);
        assertThat(groupPurchase.getEndsAt()).isEqualTo(ENDS_AT);
        assertThat(groupPurchase.getStatus()).isEqualTo(GroupPurchaseStatus.DRAFT);
    }

    @Test
    void rejectsInvalidTargetQuantityAndPeriod() {
        ProductVariant variant = createVariant();

        assertThatIllegalArgumentException()
            .isThrownBy(() -> createGroupPurchase(variant, new BigDecimal("15000"), 0, STARTS_AT, ENDS_AT))
            .withMessageContaining("Target quantity");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> createGroupPurchase(variant, new BigDecimal("15000"), 100, STARTS_AT, STARTS_AT))
            .withMessageContaining("after start time");
    }

    @Test
    void updatesDraftGroupPurchaseRecruitmentInfo() {
        ProductVariant variant = createVariant();
        GroupPurchase groupPurchase = createGroupPurchase(
            variant,
            new BigDecimal("15000"),
            100,
            STARTS_AT,
            ENDS_AT
        );
        Instant newStartsAt = Instant.parse("2026-07-21T00:00:00Z");
        Instant newEndsAt = Instant.parse("2026-07-28T00:00:00Z");

        groupPurchase.updateRecruitmentInfo(
            " Updated group purchase ",
            new BigDecimal("14000"),
            80,
            newStartsAt,
            newEndsAt
        );

        assertThat(groupPurchase.getProductVariant()).isSameAs(variant);
        assertThat(groupPurchase.getTitle()).isEqualTo("Updated group purchase");
        assertThat(groupPurchase.getGroupPrice()).isEqualByComparingTo("14000.00");
        assertThat(groupPurchase.getTargetQuantity()).isEqualTo(80);
        assertThat(groupPurchase.getStartsAt()).isEqualTo(newStartsAt);
        assertThat(groupPurchase.getEndsAt()).isEqualTo(newEndsAt);
        assertThat(groupPurchase.getStatus()).isEqualTo(GroupPurchaseStatus.DRAFT);
    }

    @Test
    void rejectsUpdateAfterGroupPurchaseOpens() {
        Product product = Product.create("Ethiopia Guji", null);
        ProductVariant variant = product.addVariant("GUJI-200G", "200g", new BigDecimal("20000"));
        product.activate();
        GroupPurchase groupPurchase = createGroupPurchase(
            variant,
            new BigDecimal("15000"),
            100,
            STARTS_AT,
            ENDS_AT
        );
        groupPurchase.open(STARTS_AT);

        assertThatIllegalStateException()
            .isThrownBy(() -> groupPurchase.updateRecruitmentInfo(
                "Updated group purchase",
                new BigDecimal("14000"),
                80,
                STARTS_AT,
                ENDS_AT
            ))
            .withMessage("Only a draft group purchase can be updated");
    }

    @Test
    void opensAndClosesGroupPurchase() {
        Product product = Product.create("Ethiopia Guji", null);
        ProductVariant variant = product.addVariant("GUJI-200G", "200g", new BigDecimal("20000"));
        product.activate();
        GroupPurchase groupPurchase = createGroupPurchase(
            variant,
            new BigDecimal("15000"),
            100,
            STARTS_AT,
            ENDS_AT
        );

        groupPurchase.open(STARTS_AT);
        assertThat(groupPurchase.getStatus()).isEqualTo(GroupPurchaseStatus.OPEN);
        groupPurchase.close();
        assertThat(groupPurchase.getStatus()).isEqualTo(GroupPurchaseStatus.CLOSED);
    }

    @Test
    void cancelsDraftGroupPurchase() {
        GroupPurchase draft = createGroupPurchase(
            createVariant(),
            new BigDecimal("15000"),
            100,
            STARTS_AT,
            ENDS_AT
        );
        draft.cancel();

        assertThat(draft.getStatus()).isEqualTo(GroupPurchaseStatus.CANCELLED);
    }

    @Test
    void cancelsOpenGroupPurchase() {
        Product product = Product.create("Ethiopia Guji", null);
        ProductVariant variant = product.addVariant("CANCEL-GUJI-200G", "200g", new BigDecimal("20000"));
        product.activate();
        GroupPurchase groupPurchase = createGroupPurchase(
            variant,
            new BigDecimal("15000"),
            100,
            STARTS_AT,
            ENDS_AT
        );
        groupPurchase.open(STARTS_AT);

        groupPurchase.cancel();

        assertThat(groupPurchase.getStatus()).isEqualTo(GroupPurchaseStatus.CANCELLED);
    }

    @Test
    void rejectsCancelAfterGroupPurchaseCloses() {
        Product product = Product.create("Ethiopia Guji", null);
        ProductVariant variant = product.addVariant("CLOSED-GUJI-200G", "200g", new BigDecimal("20000"));
        product.activate();
        GroupPurchase groupPurchase = createGroupPurchase(
            variant,
            new BigDecimal("15000"),
            100,
            STARTS_AT,
            ENDS_AT
        );
        groupPurchase.open(STARTS_AT);
        groupPurchase.close();

        assertThatIllegalStateException()
            .isThrownBy(groupPurchase::cancel)
            .withMessage("Only a draft or open group purchase can be cancelled");
    }

    @Test
    void onlyAllowsDeletingDraftGroupPurchase() {
        GroupPurchase draft = createGroupPurchase(
            createVariant(),
            new BigDecimal("15000"),
            100,
            STARTS_AT,
            ENDS_AT
        );

        assertThatCode(draft::validateDeletion).doesNotThrowAnyException();

        Product product = Product.create("Ethiopia Guji", null);
        ProductVariant variant = product.addVariant("DELETE-GUJI-200G", "200g", new BigDecimal("20000"));
        product.activate();
        GroupPurchase open = createGroupPurchase(
            variant,
            new BigDecimal("15000"),
            100,
            STARTS_AT,
            ENDS_AT
        );
        open.open(STARTS_AT);

        assertThatIllegalStateException()
            .isThrownBy(open::validateDeletion)
            .withMessage("Only a draft group purchase can be deleted");
    }

    private static ProductVariant createVariant() {
        Product product = Product.create("Ethiopia Guji", null);
        return product.addVariant("GUJI-200G", "200g", new BigDecimal("20000"));
    }

    private static GroupPurchase createGroupPurchase(
        ProductVariant variant,
        BigDecimal groupPrice,
        int targetQuantity,
        Instant startsAt,
        Instant endsAt
    ) {
        return GroupPurchase.create(
            variant,
            "Ethiopia Guji group purchase",
            groupPrice,
            targetQuantity,
            startsAt,
            endsAt
        );
    }
}
