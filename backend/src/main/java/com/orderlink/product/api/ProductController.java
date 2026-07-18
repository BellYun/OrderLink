package com.orderlink.product.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/{productId}")
    public ProductDetailResponse getDetail(@PathVariable Long productId) {
        return ProductDetailResponse.from(productService.getDetail(productId));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<Void> update(
        @PathVariable Long productId,
        @Valid @RequestBody ProductUpdateRequest request
    ) {
        productService.update(productId, request.toCommand());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{productId}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long productId) {
        productService.activate(productId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{productId}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long productId) {
        productService.deactivate(productId);
        return ResponseEntity.noContent().build();
    }
}
