package com.orderlink.product.application;

import java.math.BigDecimal;
import java.util.List;

public record ProductCreateCommand(
    String name,
    String description,
    List<Variant> variants
) {

    public ProductCreateCommand {
        variants = List.copyOf(variants);
    }

    public record Variant(
        String skuCode,
        String name,
        BigDecimal price
    ) {
    }
}
