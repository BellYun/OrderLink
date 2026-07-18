package com.orderlink.grouppurchase.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orderlink.grouppurchase.application.GroupPurchaseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/group-purchases")
public class GroupPurchaseController {

    private final GroupPurchaseService groupPurchaseService;

    public GroupPurchaseController(GroupPurchaseService groupPurchaseService) {
        this.groupPurchaseService = groupPurchaseService;
    }

    @PostMapping
    public ResponseEntity<GroupPurchaseCreateResponse> create(
        @Valid @RequestBody GroupPurchaseCreateRequest request
    ) {
        Long groupPurchaseId = groupPurchaseService.create(request.toCommand());
        URI location = URI.create("/api/v1/group-purchases/" + groupPurchaseId);

        return ResponseEntity.created(location)
            .body(new GroupPurchaseCreateResponse(groupPurchaseId));
    }
}
