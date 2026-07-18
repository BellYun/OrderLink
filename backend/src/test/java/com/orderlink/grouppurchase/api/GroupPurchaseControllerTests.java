package com.orderlink.grouppurchase.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.orderlink.common.api.GlobalExceptionHandler;
import com.orderlink.grouppurchase.application.GroupPurchaseCreateCommand;
import com.orderlink.grouppurchase.application.GroupPurchaseDetailResult;
import com.orderlink.grouppurchase.application.GroupPurchaseListResult;
import com.orderlink.grouppurchase.application.GroupPurchaseService;
import com.orderlink.grouppurchase.domain.GroupPurchaseStatus;
import com.orderlink.product.application.ProductVariantNotFoundException;

@WebMvcTest(GroupPurchaseController.class)
@Import(GlobalExceptionHandler.class)
class GroupPurchaseControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GroupPurchaseService groupPurchaseService;

    @Test
    void createsGroupPurchase() throws Exception {
        given(groupPurchaseService.create(any(GroupPurchaseCreateCommand.class))).willReturn(42L);

        mockMvc.perform(post("/api/v1/group-purchases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest()))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/v1/group-purchases/42"))
            .andExpect(jsonPath("$.id").value(42));

        verify(groupPurchaseService).create(any(GroupPurchaseCreateCommand.class));
    }

    @Test
    void returnsValidationErrorForInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/group-purchases")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "productVariantId": 0,
                      "title": " ",
                      "groupPrice": 0,
                      "targetQuantity": 0
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(groupPurchaseService);
    }

    @Test
    void returnsNotFoundWhenProductVariantDoesNotExist() throws Exception {
        given(groupPurchaseService.create(any(GroupPurchaseCreateCommand.class)))
            .willThrow(new ProductVariantNotFoundException(100L));

        mockMvc.perform(post("/api/v1/group-purchases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.code").value("PRODUCT_VARIANT_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("Product variant not found: 100"))
            .andExpect(jsonPath("$.path").value("/api/v1/group-purchases"));
    }

    @Test
    void opensGroupPurchase() throws Exception {
        mockMvc.perform(patch("/api/v1/group-purchases/{groupPurchaseId}/open", 42L))
            .andExpect(status().isNoContent());

        verify(groupPurchaseService).open(42L);
    }

    @Test
    void closesGroupPurchase() throws Exception {
        mockMvc.perform(patch("/api/v1/group-purchases/{groupPurchaseId}/close", 42L))
            .andExpect(status().isNoContent());

        verify(groupPurchaseService).close(42L);
    }

    @Test
    void returnsGroupPurchaseDetail() throws Exception {
        Instant startsAt = Instant.parse("2026-07-20T00:00:00Z");
        Instant endsAt = Instant.parse("2026-07-27T00:00:00Z");
        given(groupPurchaseService.getDetail(42L)).willReturn(new GroupPurchaseDetailResult(
            42L,
            "Ethiopia Guji group purchase",
            GroupPurchaseStatus.DRAFT,
            new BigDecimal("15000.00"),
            100,
            startsAt,
            endsAt,
            10L,
            "Ethiopia Guji",
            100L,
            "GUJI-200G",
            "200g",
            new BigDecimal("20000.00"),
            startsAt,
            startsAt
        ));

        mockMvc.perform(get("/api/v1/group-purchases/{groupPurchaseId}", 42L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(42))
            .andExpect(jsonPath("$.productName").value("Ethiopia Guji"))
            .andExpect(jsonPath("$.skuCode").value("GUJI-200G"))
            .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void returnsPagedGroupPurchases() throws Exception {
        Instant startsAt = Instant.parse("2026-07-20T00:00:00Z");
        GroupPurchaseListResult result = new GroupPurchaseListResult(
            List.of(new GroupPurchaseListResult.Item(
                42L,
                100L,
                "Ethiopia Guji group purchase",
                GroupPurchaseStatus.OPEN,
                new BigDecimal("15000.00"),
                100,
                startsAt,
                Instant.parse("2026-07-27T00:00:00Z"),
                startsAt
            )),
            0,
            20,
            1,
            1
        );
        given(groupPurchaseService.getList(eq(GroupPurchaseStatus.OPEN), any(Pageable.class)))
            .willReturn(result);

        mockMvc.perform(get("/api/v1/group-purchases")
                .param("status", "open"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].id").value(42))
            .andExpect(jsonPath("$.items[0].status").value("OPEN"))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void returnsBadRequestForUnsupportedStatus() throws Exception {
        mockMvc.perform(get("/api/v1/group-purchases")
                .param("status", "pending"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        verifyNoInteractions(groupPurchaseService);
    }

    private static String validRequest() {
        return """
            {
              "productVariantId": 100,
              "title": "Ethiopia Guji group purchase",
              "groupPrice": 15000,
              "targetQuantity": 100,
              "startsAt": "2026-07-20T00:00:00Z",
              "endsAt": "2026-07-27T00:00:00Z"
            }
            """;
    }
}
