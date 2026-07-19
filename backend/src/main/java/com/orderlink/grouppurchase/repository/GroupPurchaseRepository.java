package com.orderlink.grouppurchase.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orderlink.grouppurchase.domain.GroupPurchase;
import com.orderlink.grouppurchase.domain.GroupPurchaseStatus;

public interface GroupPurchaseRepository extends JpaRepository<GroupPurchase, Long> {

    Page<GroupPurchase> findAllByStatus(GroupPurchaseStatus status, Pageable pageable);

    boolean existsByProductVariantProductId(Long productId);

    @Query("""
        select groupPurchase
        from GroupPurchase groupPurchase
        join fetch groupPurchase.productVariant variant
        join fetch variant.product
        where groupPurchase.id = :groupPurchaseId
        """)
    Optional<GroupPurchase> findDetailById(@Param("groupPurchaseId") Long groupPurchaseId);
}
