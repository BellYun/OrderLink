package com.orderlink.product.application;

public class ProductVariantNotFoundException extends RuntimeException {

    public ProductVariantNotFoundException(Long productVariantId) {
        super("Product variant not found: " + productVariantId);
    }
}
