package com.orderlink.grouppurchase.application;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orderlink.grouppurchase.domain.GroupPurchase;
import com.orderlink.grouppurchase.domain.GroupPurchaseStatus;
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

    @Transactional
    public void update(Long groupPurchaseId, GroupPurchaseUpdateCommand command) {
        GroupPurchase groupPurchase = getGroupPurchase(groupPurchaseId);
        groupPurchase.updateRecruitmentInfo(
            command.title(),
            command.groupPrice(),
            command.targetQuantity(),
            command.startsAt(),
            command.endsAt()
        );
    }

    @Transactional
    public void open(Long groupPurchaseId) {
        GroupPurchase groupPurchase = getGroupPurchase(groupPurchaseId);
        groupPurchase.open(Instant.now());
    }

    @Transactional
    public void close(Long groupPurchaseId) {
        GroupPurchase groupPurchase = getGroupPurchase(groupPurchaseId);
        groupPurchase.close();
    }

    @Transactional
    public void cancel(Long groupPurchaseId) {
        GroupPurchase groupPurchase = getGroupPurchase(groupPurchaseId);
        groupPurchase.cancel();
    }

    @Transactional
    public void delete(Long groupPurchaseId) {
        GroupPurchase groupPurchase = getGroupPurchase(groupPurchaseId);
        groupPurchase.validateDeletion();
        groupPurchaseRepository.delete(groupPurchase);
    }

    @Transactional(readOnly = true)
    public GroupPurchaseDetailResult getDetail(Long groupPurchaseId) {
        GroupPurchase groupPurchase = groupPurchaseRepository.findDetailById(groupPurchaseId)
            .orElseThrow(() -> new GroupPurchaseNotFoundException(groupPurchaseId));

        return GroupPurchaseDetailResult.from(groupPurchase);
    }

    @Transactional(readOnly = true)
    public GroupPurchaseListResult getList(GroupPurchaseStatus status, Pageable pageable) {
        Page<GroupPurchase> groupPurchases = status == null
            ? groupPurchaseRepository.findAll(pageable)
            : groupPurchaseRepository.findAllByStatus(status, pageable);

        return GroupPurchaseListResult.from(groupPurchases);
    }

    private GroupPurchase getGroupPurchase(Long groupPurchaseId) {
        return groupPurchaseRepository.findById(groupPurchaseId)
            .orElseThrow(() -> new GroupPurchaseNotFoundException(groupPurchaseId));
    }
}
