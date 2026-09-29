package np.com.thapanarayan.ecommerce.service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import np.com.thapanarayan.ecommerce.domain.entity.Brand;
import np.com.thapanarayan.ecommerce.dto.request.BrandRequest;
import np.com.thapanarayan.ecommerce.dto.response.BrandResponse;
import np.com.thapanarayan.ecommerce.exception.BadRequestException;
import np.com.thapanarayan.ecommerce.exception.ResourceNotFoundException;
import np.com.thapanarayan.ecommerce.repository.BrandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public BrandResponse createBrand(BrandRequest request) {
        String slug = generateSlug(request.getName(), request.getSlug());

        if (brandRepository.existsByName(request.getName().trim())) {
            throw new BadRequestException("Brand with name '" + request.getName() + "' already exists");
        }
        if (brandRepository.existsBySlug(slug)) {
            throw new BadRequestException("Brand with slug '" + slug + "' already exists");
        }

        Brand brand = new Brand(
                UUID.randomUUID(),
                request.getName().trim(),
                slug,
                request.getDescription()
        );

        Brand saved = brandRepository.save(brand);
        return BrandResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(BrandResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BrandResponse getBrandById(UUID id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
        return BrandResponse.fromEntity(brand);
    }

    public BrandResponse updateBrand(UUID id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));

        String trimmedName = request.getName().trim();
        if (!brand.getName().equalsIgnoreCase(trimmedName) && brandRepository.existsByName(trimmedName)) {
            throw new BadRequestException("Brand with name '" + trimmedName + "' already exists");
        }

        String slug = generateSlug(trimmedName, request.getSlug());
        if (!brand.getSlug().equalsIgnoreCase(slug) && brandRepository.existsBySlug(slug)) {
            throw new BadRequestException("Brand with slug '" + slug + "' already exists");
        }

        brand.setName(trimmedName);
        brand.setSlug(slug);
        brand.setDescription(request.getDescription());
        brand.setUpdatedAt(Instant.now());

        Brand updated = brandRepository.save(brand);
        return BrandResponse.fromEntity(updated);
    }

    public void deleteBrand(UUID id) {
        if (!brandRepository.existsById(id)) {
            throw new ResourceNotFoundException("Brand not found with id: " + id);
        }
        brandRepository.deleteById(id);
    }

    private String generateSlug(String name, String customSlug) {
        if (customSlug != null && !customSlug.trim().isEmpty()) {
            return customSlug.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        }
        return name.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
}
