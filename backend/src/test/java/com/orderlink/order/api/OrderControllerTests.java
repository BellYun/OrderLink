package com.orderlink.order.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.orderlink.common.api.GlobalExceptionHandler;
import com.orderlink.grouppurchase.application.GroupPurchaseNotFoundException;
import com.orderlink.order.application.OrderCreateCommand;
import com.orderlink.order.application.OrderService;

@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void createsOrder() throws Exception {
        given(orderService.create(any(OrderCreateCommand.class))).willReturn(42L);

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest()))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/v1/orders/42"))
            .andExpect(jsonPath("$.id").value(42));

        verify(orderService).create(new OrderCreateCommand(10L, "buyer-1", 2));
    }

    @Test
    void returnsValidationErrorForInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "groupPurchaseId": 0,
                      "buyerId": " ",
                      "quantity": 0
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(orderService);
    }

    @Test
    void returnsNotFoundWhenGroupPurchaseDoesNotExist() throws Exception {
        given(orderService.create(any(OrderCreateCommand.class)))
            .willThrow(new GroupPurchaseNotFoundException(10L));

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("GROUP_PURCHASE_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("Group purchase not found: 10"));
    }

    @Test
    void returnsBadRequestWhenGroupPurchaseCannotReceiveOrders() throws Exception {
        given(orderService.create(any(OrderCreateCommand.class)))
            .willThrow(new IllegalStateException("Only an open group purchase can receive orders"));

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
            .andExpect(jsonPath("$.message").value("Only an open group purchase can receive orders"));
    }

    private static String validRequest() {
        return """
            {
              "groupPurchaseId": 10,
              "buyerId": "buyer-1",
              "quantity": 2
            }
            """;
    }
}
