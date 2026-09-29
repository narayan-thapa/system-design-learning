package np.com.thapanarayan.ecommerce.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import np.com.thapanarayan.ecommerce.dto.GenericResponse;
import np.com.thapanarayan.ecommerce.dto.request.ProductRequest;
import np.com.thapanarayan.ecommerce.dto.response.ProductResponse;
import np.com.thapanarayan.ecommerce.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<GenericResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.created(URI.create("/api/v1/products/" + response.getId()))
                .body(GenericResponse.success("Product created successfully", response));
    }

    @GetMapping
    public ResponseEntity<GenericResponse<Page<ProductResponse>>> getProducts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID brandId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(GenericResponse.success(productService.getProducts(categoryId, brandId, search, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse<ProductResponse>> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(GenericResponse.success(productService.getProductById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse<ProductResponse>> updateProduct(@PathVariable UUID id,
                                                                         @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(GenericResponse.success("Product updated successfully", productService.updateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse<Void>> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(GenericResponse.success("Product deleted successfully", null));
    }
}
