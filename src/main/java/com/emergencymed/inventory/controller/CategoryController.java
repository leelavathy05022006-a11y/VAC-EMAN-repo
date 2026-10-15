package com.emergencymed.inventory.controller;

import com.emergencymed.inventory.dto.ApiResponse;
import com.emergencymed.inventory.dto.CategoryDto;
import com.emergencymed.inventory.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<List<CategoryDto>> getCategories() {
        return ApiResponse.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryDto> getCategoryById(@PathVariable Long id) {
        return ApiResponse.ok(categoryService.getCategoryById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CategoryDto> createCategory(@Valid @RequestBody CategoryDto dto) {
        return ApiResponse.ok(categoryService.createCategory(dto), "Category created successfully");
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryDto> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDto dto) {
        return ApiResponse.ok(categoryService.updateCategory(id, dto), "Category updated successfully");
    }
}
