package com.orderlink.product.application;

public record ProductUpdateCommand(
    String name,
    String description
) {
}
