package com.orderlink.product.api;

import com.orderlink.product.application.ProductUpdateCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductUpdateRequest(
    @NotBlank @Size(max = 100) String name,
    String description
) {

    public ProductUpdateCommand toCommand() {
        return new ProductUpdateCommand(name, description);
    }
}
