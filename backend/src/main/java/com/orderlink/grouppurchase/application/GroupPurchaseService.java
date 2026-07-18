package com.orderlink.grouppurchase.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orderlink.grouppurchase.domain.GroupPurchase;
import com.orderlink.grouppurchase.repository.GroupPurchaseRepository;
import com.orderlink.product.application.ProductVariantNotFoundException;
import com.orderlink.product.domain.ProductVariant;
import com.orderlink.product.repository.ProductVariantRepository;

@Service
public class GroupPurchaseService {

    private final GroupPurchaseRepository groupPurchaseRepository;
    private final ProductVariantRepository productVariantRepository;

    public GroupPurchaseService(
        GroupPurchaseRepository groupPurchaseRepository,
        ProductVariantRepository productVariantRepository
    ) {
        this.groupPurchaseRepository = groupPurchaseRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Transactional
    public Long create(GroupPurchaseCreateCommand command) {
        ProductVariant productVariant = productVariantRepository.findById(command.productVariantId())
            .orElseThrow(() -> new ProductVariantNotFoundException(command.productVariantId()));

        GroupPurchase groupPurchase = GroupPurchase.create(
            productVariant,
            command.title(),
            command.groupPrice(),
            command.targetQuantity(),
            command.startsAt(),
            command.endsAt()
        );

        return groupPurchaseRepository.saveAndFlush(groupPurchase).getId();
    }
}
