package com.orderlink.grouppurchase.application;

public class GroupPurchaseNotFoundException extends RuntimeException {

    public GroupPurchaseNotFoundException(Long groupPurchaseId) {
        super("Group purchase not found: " + groupPurchaseId);
    }
}
