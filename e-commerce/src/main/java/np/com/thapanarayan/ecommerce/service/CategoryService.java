package np.com.thapanarayan.ecommerce.service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import np.com.thapanarayan.ecommerce.domain.entity.Category;
import np.com.thapanarayan.ecommerce.dto.request.CategoryRequest;
import np.com.thapanarayan.ecommerce.dto.response.CategoryResponse;
import np.com.thapanarayan.ecommerce.exception.BadRequestException;
import np.com.thapanarayan.ecommerce.exception.ResourceNotFoundException;
import np.com.thapanarayan.ecommerce.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        String slug = generateSlug(request.getName(), request.getSlug());

        if (categoryRepository.existsByName(request.getName().trim())) {
            throw new BadRequestException("Category with name '" + request.getName() + "' already exists");
        }
        if (categoryRepository.existsBySlug(slug)) {
            throw new BadRequestException("Category with slug '" + slug + "' already exists");
        }

        Category category = new Category(
                UUID.randomUUID(),
                request.getName().trim(),
                slug,
                request.getDescription()
        );

        Category saved = categoryRepository.save(category);
        return CategoryResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return CategoryResponse.fromEntity(category);
    }

    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        String trimmedName = request.getName().trim();
        if (!category.getName().equalsIgnoreCase(trimmedName) && categoryRepository.existsByName(trimmedName)) {
            throw new BadRequestException("Category with name '" + trimmedName + "' already exists");
        }

        String slug = generateSlug(trimmedName, request.getSlug());
        if (!category.getSlug().equalsIgnoreCase(slug) && categoryRepository.existsBySlug(slug)) {
            throw new BadRequestException("Category with slug '" + slug + "' already exists");
        }

        category.setName(trimmedName);
        category.setSlug(slug);
        category.setDescription(request.getDescription());
        category.setUpdatedAt(Instant.now());

        Category updated = categoryRepository.save(category);
        return CategoryResponse.fromEntity(updated);
    }

    public void deleteCategory(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    private String generateSlug(String name, String customSlug) {
        if (customSlug != null && !customSlug.trim().isEmpty()) {
            return customSlug.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        }
        return name.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
}
