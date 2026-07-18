package com.orderlink.grouppurchase.api;

import java.net.URI;
import java.util.Locale;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.orderlink.grouppurchase.application.GroupPurchaseService;
import com.orderlink.grouppurchase.domain.GroupPurchaseStatus;

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

    @PatchMapping("/{groupPurchaseId}")
    public ResponseEntity<Void> update(
        @PathVariable Long groupPurchaseId,
        @Valid @RequestBody GroupPurchaseUpdateRequest request
    ) {
        groupPurchaseService.update(groupPurchaseId, request.toCommand());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{groupPurchaseId}/open")
    public ResponseEntity<Void> open(@PathVariable Long groupPurchaseId) {
        groupPurchaseService.open(groupPurchaseId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{groupPurchaseId}/close")
    public ResponseEntity<Void> close(@PathVariable Long groupPurchaseId) {
        groupPurchaseService.close(groupPurchaseId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{groupPurchaseId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long groupPurchaseId) {
        groupPurchaseService.cancel(groupPurchaseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupPurchaseId}")
    public GroupPurchaseDetailResponse getDetail(@PathVariable Long groupPurchaseId) {
        return GroupPurchaseDetailResponse.from(groupPurchaseService.getDetail(groupPurchaseId));
    }

    @GetMapping
    public GroupPurchaseListResponse getList(
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        validatePage(page, size);
        GroupPurchaseStatus parsedStatus = parseStatus(status);
        PageRequest pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "createdAt", "id")
        );

        return GroupPurchaseListResponse.from(groupPurchaseService.getList(parsedStatus, pageable));
    }

    private static GroupPurchaseStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return GroupPurchaseStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported group purchase status: " + status, exception);
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
