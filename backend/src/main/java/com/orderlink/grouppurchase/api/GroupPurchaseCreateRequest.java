package com.orderlink.grouppurchase.api;

import java.math.BigDecimal;
import java.time.Instant;

import com.orderlink.grouppurchase.application.GroupPurchaseCreateCommand;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record GroupPurchaseCreateRequest(
    @NotNull @Positive Long productVariantId,
    @NotBlank @Size(max = 100) String title,
    @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal groupPrice,
    @NotNull @Positive Integer targetQuantity,
    @NotNull Instant startsAt,
    @NotNull Instant endsAt
) {

    public GroupPurchaseCreateCommand toCommand() {
        return new GroupPurchaseCreateCommand(
            productVariantId,
            title,
            groupPrice,
            targetQuantity,
            startsAt,
            endsAt
        );
    }
}
