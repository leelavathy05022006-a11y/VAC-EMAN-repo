package com.emergencymed.inventory.controller;

import com.emergencymed.inventory.dto.ApiResponse;
import com.emergencymed.inventory.dto.MedicineDto;
import com.emergencymed.inventory.dto.PageResponse;
import com.emergencymed.inventory.service.MedicineService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/medicines", "/api/medicines"})
public class MedicineController {
    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping
    public ApiResponse<PageResponse<MedicineDto>> searchMedicines(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean requiresPrescription,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort) {
        int max = Math.min(Math.max(size, 1), 50);
        PageResponse<MedicineDto> result = PageResponse.from(
                medicineService.searchMedicines(q, categoryId, requiresPrescription, PageRequest.of(page, max, Sort.by(sort)))
        );
        return ApiResponse.ok(result);
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<MedicineDto>> searchByArea(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String area,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<MedicineDto> result = PageResponse.from(
                medicineService.searchMedicines(name, null, null, PageRequest.of(page, size))
        );
        return ApiResponse.ok(result);
    }

    @GetMapping("/featured")
    public ApiResponse<List<MedicineDto>> getFeaturedMedicines() {
        PageResponse<MedicineDto> result = PageResponse.from(
                medicineService.searchMedicines(null, null, null, PageRequest.of(0, 8, Sort.by("name")))
        );
        return ApiResponse.ok(result.content());
    }

    @GetMapping("/{id}")
    public ApiResponse<MedicineDto> getMedicineById(@PathVariable Long id) {
        return ApiResponse.ok(medicineService.getMedicineById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MedicineDto> createMedicine(@Valid @RequestBody MedicineDto dto) {
        return ApiResponse.ok(medicineService.createMedicine(dto), "Medicine created successfully");
    }

    @PutMapping("/{id}")
    public ApiResponse<MedicineDto> updateMedicine(@PathVariable Long id, @Valid @RequestBody MedicineDto dto) {
        return ApiResponse.ok(medicineService.updateMedicine(id, dto), "Medicine updated successfully");
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<MedicineDto> updateStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        Boolean active = body.getOrDefault("isActive", true);
        return ApiResponse.ok(medicineService.updateStatus(id, active), "Medicine status updated");
    }
}
