package np.com.thapanarayan.ecommerce.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import np.com.thapanarayan.ecommerce.dto.GenericResponse;
import np.com.thapanarayan.ecommerce.dto.request.CategoryRequest;
import np.com.thapanarayan.ecommerce.dto.response.CategoryResponse;
import np.com.thapanarayan.ecommerce.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<GenericResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.created(URI.create("/api/v1/categories/" + response.getId()))
                .body(GenericResponse.success("Category created successfully", response));
    }

    @GetMapping
    public ResponseEntity<GenericResponse<List<CategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(GenericResponse.success(categoryService.getAllCategories()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse<CategoryResponse>> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(GenericResponse.success(categoryService.getCategoryById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse<CategoryResponse>> updateCategory(@PathVariable UUID id,
                                                                           @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(GenericResponse.success("Category updated successfully", categoryService.updateCategory(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse<Void>> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(GenericResponse.success("Category deleted successfully", null));
    }
}
