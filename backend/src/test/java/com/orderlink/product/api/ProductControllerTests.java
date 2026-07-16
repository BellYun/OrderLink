package com.orderlink.product.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.orderlink.common.api.GlobalExceptionHandler;
import com.orderlink.product.application.DuplicateSkuException;
import com.orderlink.product.application.ProductCreateCommand;
import com.orderlink.product.application.ProductDetailResult;
import com.orderlink.product.application.ProductNotFoundException;
import com.orderlink.product.application.ProductService;
import com.orderlink.product.domain.ProductStatus;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void createsProduct() throws Exception {
        given(productService.create(any(ProductCreateCommand.class))).willReturn(42L);

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Ethiopia Guji",
                      "description": "Natural process coffee beans",
                      "variants": [
                        {
                          "skuCode": "GUJI-200G-BEAN",
                          "name": "200g whole bean",
                          "price": 18000
                        }
                      ]
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/v1/products/42"))
            .andExpect(jsonPath("$.id").value(42));

        ArgumentCaptor<ProductCreateCommand> commandCaptor = ArgumentCaptor.forClass(ProductCreateCommand.class);
        verify(productService).create(commandCaptor.capture());

        ProductCreateCommand command = commandCaptor.getValue();
        assertThat(command.name()).isEqualTo("Ethiopia Guji");
        assertThat(command.variants()).hasSize(1);
        assertThat(command.variants().getFirst().skuCode()).isEqualTo("GUJI-200G-BEAN");
    }

    @Test
    void returnsProductDetail() throws Exception {
        Instant createdAt = Instant.parse("2026-07-16T06:00:00Z");
        Instant updatedAt = Instant.parse("2026-07-16T06:10:00Z");
        ProductDetailResult result = new ProductDetailResult(
            42L,
            "Kenya Nyeri",
            "Washed process coffee beans",
            ProductStatus.DRAFT,
            List.of(new ProductDetailResult.Variant(
                100L,
                "NYERI-200G",
                "200g whole bean",
                new BigDecimal("19000.00")
            )),
            createdAt,
            updatedAt
        );
        given(productService.getDetail(42L)).willReturn(result);

        mockMvc.perform(get("/api/v1/products/{productId}", 42L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(42))
            .andExpect(jsonPath("$.name").value("Kenya Nyeri"))
            .andExpect(jsonPath("$.description").value("Washed process coffee beans"))
            .andExpect(jsonPath("$.status").value("DRAFT"))
            .andExpect(jsonPath("$.variants[0].id").value(100))
            .andExpect(jsonPath("$.variants[0].skuCode").value("NYERI-200G"))
            .andExpect(jsonPath("$.variants[0].name").value("200g whole bean"))
            .andExpect(jsonPath("$.variants[0].price").value(19000.00))
            .andExpect(jsonPath("$.createdAt").value("2026-07-16T06:00:00Z"))
            .andExpect(jsonPath("$.updatedAt").value("2026-07-16T06:10:00Z"));

        verify(productService).getDetail(42L);
    }

    @Test
    void returnsNotFoundWhenProductDoesNotExist() throws Exception {
        given(productService.getDetail(999L)).willThrow(new ProductNotFoundException(999L));

        mockMvc.perform(get("/api/v1/products/{productId}", 999L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("Product not found: 999"))
            .andExpect(jsonPath("$.path").value("/api/v1/products/999"));
    }

    @Test
    void returnsValidationErrorForInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": " ",
                      "variants": []
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.path").value("/api/v1/products"))
            .andExpect(jsonPath("$.fieldErrors.name").exists())
            .andExpect(jsonPath("$.fieldErrors.variants").exists());

        verifyNoInteractions(productService);
    }

    @Test
    void validatesFieldsInsideVariant() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Ethiopia Guji",
                      "variants": [
                        {
                          "skuCode": " ",
                          "name": " ",
                          "price": -1
                        }
                      ]
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.fieldErrors['variants[0].skuCode']").exists())
            .andExpect(jsonPath("$.fieldErrors['variants[0].name']").exists())
            .andExpect(jsonPath("$.fieldErrors['variants[0].price']").exists());

        verifyNoInteractions(productService);
    }

    @Test
    void returnsConflictForDuplicateSku() throws Exception {
        given(productService.create(any(ProductCreateCommand.class)))
            .willThrow(new DuplicateSkuException("GUJI-200G-BEAN"));

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Ethiopia Guji",
                      "variants": [
                        {
                          "skuCode": "GUJI-200G-BEAN",
                          "name": "200g whole bean",
                          "price": 18000
                        }
                      ]
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.code").value("DUPLICATE_SKU"))
            .andExpect(jsonPath("$.message").value("SKU code already exists: GUJI-200G-BEAN"))
            .andExpect(jsonPath("$.path").value("/api/v1/products"));
    }
}
