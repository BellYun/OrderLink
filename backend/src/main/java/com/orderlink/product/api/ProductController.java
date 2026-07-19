package com.orderlink.product.api;

import java.net.URI;
import java.util.Locale;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.orderlink.product.application.ProductService;
import com.orderlink.product.domain.ProductStatus;

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

    @GetMapping
    public ProductListResponse getList(
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        validatePage(page, size);
        ProductStatus parsedStatus = parseStatus(status);
        PageRequest pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "createdAt", "id")
        );

        return ProductListResponse.from(productService.getList(parsedStatus, pageable));
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

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@PathVariable Long productId) {
        productService.delete(productId);
        return ResponseEntity.noContent().build();
    }

    private static ProductStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return ProductStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported product status: " + status, exception);
        }
    }

    private static void validatePage(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must not be negative");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }
    }
}
