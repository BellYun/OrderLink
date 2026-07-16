package com.orderlink.product.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.orderlink.product.application.ProductService;

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
