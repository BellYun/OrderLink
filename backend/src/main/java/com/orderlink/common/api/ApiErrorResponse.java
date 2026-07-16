package com.orderlink.common.api;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
    Instant timestamp,
    int status,
    String code,
    String message,
    String path,
    Map<String, String> fieldErrors
) {

    public ApiErrorResponse {
        fieldErrors = Map.copyOf(fieldErrors);
    }
}
