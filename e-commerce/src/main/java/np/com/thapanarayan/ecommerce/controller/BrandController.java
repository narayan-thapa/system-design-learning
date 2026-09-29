package np.com.thapanarayan.ecommerce.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import np.com.thapanarayan.ecommerce.dto.GenericResponse;
import np.com.thapanarayan.ecommerce.dto.request.BrandRequest;
import np.com.thapanarayan.ecommerce.dto.response.BrandResponse;
import np.com.thapanarayan.ecommerce.service.BrandService;
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
@RequestMapping("/api/v1/brands")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping
    public ResponseEntity<GenericResponse<BrandResponse>> createBrand(@Valid @RequestBody BrandRequest request) {
        BrandResponse response = brandService.createBrand(request);
        return ResponseEntity.created(URI.create("/api/v1/brands/" + response.getId()))
                .body(GenericResponse.success("Brand created successfully", response));
    }

    @GetMapping
    public ResponseEntity<GenericResponse<List<BrandResponse>>> getAllBrands() {
        return ResponseEntity.ok(GenericResponse.success(brandService.getAllBrands()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse<BrandResponse>> getBrandById(@PathVariable UUID id) {
        return ResponseEntity.ok(GenericResponse.success(brandService.getBrandById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse<BrandResponse>> updateBrand(@PathVariable UUID id,
                                                                     @Valid @RequestBody BrandRequest request) {
        return ResponseEntity.ok(GenericResponse.success("Brand updated successfully", brandService.updateBrand(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse<Void>> deleteBrand(@PathVariable UUID id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok(GenericResponse.success("Brand deleted successfully", null));
    }
}
