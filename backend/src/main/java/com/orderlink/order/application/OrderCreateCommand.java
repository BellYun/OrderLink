package com.orderlink.order.application;

public record OrderCreateCommand(
    Long groupPurchaseId,
    String buyerId,
    int quantity
) {
}
