package com.orderlink.product.application;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orderlink.product.domain.Product;
import com.orderlink.product.domain.ProductStatus;
import com.orderlink.product.domain.ProductVariant;
import com.orderlink.product.repository.ProductRepository;
import com.orderlink.product.repository.ProductVariantRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public ProductService(
        ProductRepository productRepository,
        ProductVariantRepository productVariantRepository
    ) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Transactional
    public Long create(ProductCreateCommand command) {
        if (command.variants().isEmpty()) {
            throw new IllegalArgumentException("At least one product variant is required");
        }

        Product product = Product.create(command.name(), command.description());
        command.variants().forEach(variant -> product.addVariant(
            variant.skuCode(),
            variant.name(),
            variant.price()
        ));

        validateSkuUniqueness(product);

        try {
            return productRepository.saveAndFlush(product).getId();
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateSkuException("One or more SKU codes already exist", exception);
        }
    }

    @Transactional(readOnly = true)
    public ProductDetailResult getDetail(Long productId) {
        Product product = productRepository.findDetailById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        return ProductDetailResult.from(product);
    }

    @Transactional(readOnly = true)
    public ProductListResult getList(ProductStatus status, Pageable pageable) {
        Page<Product> products = status == null
            ? productRepository.findAll(pageable)
            : productRepository.findAllByStatus(status, pageable);

        return ProductListResult.from(products);
    }

    @Transactional
    public void update(Long productId, ProductUpdateCommand command) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        product.updateInfo(command.name(), command.description());
    }

    @Transactional
    public void activate(Long productId) {
        Product product = productRepository.findDetailById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        product.activate();
    }

    @Transactional
    public void deactivate(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        product.deactivate();
    }

    private void validateSkuUniqueness(Product product) {
        for (ProductVariant variant : product.getVariants()) {
            if (productVariantRepository.existsBySkuCode(variant.getSkuCode())) {
                throw new DuplicateSkuException(variant.getSkuCode());
            }
        }
    }
}
