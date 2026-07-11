package com.orderlink.system;

import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
public class SystemStatusController {

    @GetMapping("/status")
    public SystemStatusResponse status() {
        return new SystemStatusResponse("UP", "orderlink-backend", Instant.now());
    }

    public record SystemStatusResponse(String status, String service, Instant timestamp) {
    }
}

