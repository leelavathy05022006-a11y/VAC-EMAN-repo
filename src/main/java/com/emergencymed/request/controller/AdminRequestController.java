package com.emergencymed.request.controller;

import com.emergencymed.common.contract.CurrentUserProvider;
import com.emergencymed.common.enums.Role;
import com.emergencymed.request.dto.ApiResponse;
import com.emergencymed.request.dto.PageResponse;
import com.emergencymed.request.dto.RequestDetailResponse;
import com.emergencymed.request.entity.EmergencyRequest;
import com.emergencymed.request.exception.AccessForbiddenException;
import com.emergencymed.request.repository.EmergencyRequestRepository;
import com.emergencymed.request.service.RequestService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/requests")
public class AdminRequestController {

    private final EmergencyRequestRepository requestRepository;
    private final RequestService requestService;
    private final CurrentUserProvider currentUserProvider;

    public AdminRequestController(
            EmergencyRequestRepository requestRepository,
            RequestService requestService,
            CurrentUserProvider currentUserProvider) {
        this.requestRepository = requestRepository;
        this.requestService = requestService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RequestDetailResponse>>> getAdminRequests(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "pharmacyId", required = false) Long pharmacyId,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        if (currentUserProvider.role() != Role.ADMIN) {
            throw new AccessForbiddenException("Only administrators can access admin request endpoints");
        }

        size = Math.min(50, Math.max(1, size));
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<EmergencyRequest> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.toUpperCase()));
            }

            if (pharmacyId != null) {
                predicates.add(cb.equal(root.get("pharmacyId"), pharmacyId));
            }

            if (from != null) {
                LocalDateTime fromTime = from.atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromTime));
            }

            if (to != null) {
                LocalDateTime toTime = to.atTime(LocalTime.MAX);
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toTime));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<EmergencyRequest> results = requestRepository.findAll(spec, pageable);
        Page<RequestDetailResponse> mapped = results.map(r -> requestService.getRequestDetails(r.getId()));

        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(mapped)));
    }
}
