package com.orderlink.grouppurchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orderlink.grouppurchase.domain.GroupPurchase;

public interface GroupPurchaseRepository extends JpaRepository<GroupPurchase, Long> {
}
