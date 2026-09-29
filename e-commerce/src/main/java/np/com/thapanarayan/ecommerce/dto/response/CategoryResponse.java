package np.com.thapanarayan.ecommerce.dto.response;

import java.time.Instant;
import java.util.UUID;
import np.com.thapanarayan.ecommerce.domain.entity.Category;
import np.com.thapanarayan.ecommerce.dto.ModelBase;

public class CategoryResponse extends ModelBase {

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private Instant createdAt;

    public CategoryResponse() {
    }

    public static CategoryResponse fromEntity(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setSlug(category.getSlug());
        response.setDescription(category.getDescription());
        response.setCreatedAt(category.getCreatedAt());
        return response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
