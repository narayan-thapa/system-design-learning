package np.com.thapanarayan.ecommerce.service;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import np.com.thapanarayan.ecommerce.domain.entity.Brand;
import np.com.thapanarayan.ecommerce.domain.entity.Category;
import np.com.thapanarayan.ecommerce.domain.entity.Product;
import np.com.thapanarayan.ecommerce.domain.enums.ProductStatus;
import np.com.thapanarayan.ecommerce.dto.request.ProductRequest;
import np.com.thapanarayan.ecommerce.dto.response.ProductResponse;
import np.com.thapanarayan.ecommerce.exception.BadRequestException;
import np.com.thapanarayan.ecommerce.exception.ResourceNotFoundException;
import np.com.thapanarayan.ecommerce.repository.BrandRepository;
import np.com.thapanarayan.ecommerce.repository.CategoryRepository;
import np.com.thapanarayan.ecommerce.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          BrandRepository brandRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
    }

    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));

        String slug = generateSlug(request.getName(), request.getSlug());
        if (productRepository.existsBySlug(slug)) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 8);
        }

        Product product = new Product(
                UUID.randomUUID(),
                request.getName().trim(),
                slug,
                request.getDescription(),
                request.getPrice(),
                request.getStockQuantity(),
                category.getId(),
                brand.getId(),
                request.getStatus() != null ? request.getStatus() : ProductStatus.ACTIVE
        );

        Product saved = productRepository.save(product);
        return ProductResponse.fromEntity(saved, category.getName(), brand.getName());
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(UUID categoryId, UUID brandId, String search, Pageable pageable) {
        Page<Product> productPage = productRepository.searchProducts(categoryId, brandId, search, pageable);

        Map<UUID, String> categoryNames = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        Map<UUID, String> brandNames = brandRepository.findAll().stream()
                .collect(Collectors.toMap(Brand::getId, Brand::getName));

        return productPage.map(product -> ProductResponse.fromEntity(
                product,
                categoryNames.getOrDefault(product.getCategoryId(), "Unknown"),
                brandNames.getOrDefault(product.getBrandId(), "Unknown")
        ));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        String categoryName = categoryRepository.findById(product.getCategoryId())
                .map(Category::getName)
                .orElse("Unknown");
        String brandName = brandRepository.findById(product.getBrandId())
                .map(Brand::getName)
                .orElse("Unknown");

        return ProductResponse.fromEntity(product, categoryName, brandName);
    }

    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));

        String slug = generateSlug(request.getName(), request.getSlug());
        if (!product.getSlug().equalsIgnoreCase(slug) && productRepository.existsBySlug(slug)) {
            throw new BadRequestException("Product with slug '" + slug + "' already exists");
        }

        product.setName(request.getName().trim());
        product.setSlug(slug);
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategoryId(category.getId());
        product.setBrandId(brand.getId());
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }
        product.setUpdatedAt(Instant.now());

        Product updated = productRepository.save(product);
        return ProductResponse.fromEntity(updated, category.getName(), brand.getName());
    }

    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    private String generateSlug(String name, String customSlug) {
        if (customSlug != null && !customSlug.trim().isEmpty()) {
            return customSlug.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        }
        return name.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
}
