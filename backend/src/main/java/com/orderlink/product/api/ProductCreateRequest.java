package com.orderlink.product.api;

import java.math.BigDecimal;
import java.util.List;

import com.orderlink.product.application.ProductCreateCommand;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductCreateRequest(
    @NotBlank @Size(max = 100) String name,
    String description,
    @NotEmpty List<@Valid Variant> variants
) {

    public ProductCreateCommand toCommand() {
        List<ProductCreateCommand.Variant> variantCommands = variants.stream()
            .map(variant -> new ProductCreateCommand.Variant(
                variant.skuCode(),
                variant.name(),
                variant.price()
            ))
            .toList();

        return new ProductCreateCommand(name, description, variantCommands);
    }

    public record Variant(
        @NotBlank @Size(max = 64) String skuCode,
        @NotBlank @Size(max = 100) String name,
        @NotNull @PositiveOrZero @Digits(integer = 17, fraction = 2) BigDecimal price
    ) {
    }
}
