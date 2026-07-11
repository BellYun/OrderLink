package com.orderlink.system;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SystemStatusControllerTests {

    private final SystemStatusController controller = new SystemStatusController();

    @Test
    void returnsServiceStatus() {
        var response = controller.status();

        assertThat(response.status()).isEqualTo("UP");
        assertThat(response.service()).isEqualTo("orderlink-backend");
        assertThat(response.timestamp()).isNotNull();
    }
}

