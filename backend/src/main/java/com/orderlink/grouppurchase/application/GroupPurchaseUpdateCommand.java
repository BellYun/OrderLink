package com.orderlink.grouppurchase.application;

import java.math.BigDecimal;
import java.time.Instant;

public record GroupPurchaseUpdateCommand(
    String title,
    BigDecimal groupPrice,
    int targetQuantity,
    Instant startsAt,
    Instant endsAt
) {
}
