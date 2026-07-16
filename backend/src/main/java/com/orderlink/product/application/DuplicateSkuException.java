package com.orderlink.product.application;

public class DuplicateSkuException extends RuntimeException {

    public DuplicateSkuException(String skuCode) {
        super("SKU code already exists: " + skuCode);
    }

    public DuplicateSkuException(String message, Throwable cause) {
        super(message, cause);
    }
}
