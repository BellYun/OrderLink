package com.orderlink.product.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orderlink.product.domain.Product;
import com.orderlink.product.domain.ProductStatus;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findAllByStatus(ProductStatus status, Pageable pageable);

    @Query("""
        select distinct product
        from Product product
        left join fetch product.variants
        where product.id = :productId
        """)
    Optional<Product> findDetailById(@Param("productId") Long productId);
}
