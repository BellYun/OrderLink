package com.orderlink.product.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orderlink.product.domain.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    boolean existsBySkuCode(String skuCode);

    Optional<ProductVariant> findBySkuCode(String skuCode);
}
