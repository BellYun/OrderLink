package com.orderlink.product.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orderlink.product.application.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductCreateResponse> create(@Valid @RequestBody ProductCreateRequest request) {
        Long productId = productService.create(request.toCommand());
        URI location = URI.create("/api/v1/products/" + productId);

        return ResponseEntity.created(location)
            .body(new ProductCreateResponse(productId));
    }
}
