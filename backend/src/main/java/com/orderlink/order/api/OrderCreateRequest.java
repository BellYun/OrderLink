package com.orderlink.order.api;

import com.orderlink.order.application.OrderCreateCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record OrderCreateRequest(
    @NotNull @Positive Long groupPurchaseId,
    @NotBlank @Size(max = 100) String buyerId,
    @NotNull @Positive Integer quantity
) {

    public OrderCreateCommand toCommand() {
        return new OrderCreateCommand(groupPurchaseId, buyerId, quantity);
    }
}
