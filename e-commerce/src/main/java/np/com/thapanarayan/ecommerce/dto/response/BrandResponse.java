package np.com.thapanarayan.ecommerce.dto.response;

import java.time.Instant;
import java.util.UUID;
import np.com.thapanarayan.ecommerce.domain.entity.Brand;
import np.com.thapanarayan.ecommerce.dto.ModelBase;

public class BrandResponse extends ModelBase {

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private Instant createdAt;

    public BrandResponse() {
    }

    public static BrandResponse fromEntity(Brand brand) {
        BrandResponse response = new BrandResponse();
        response.setId(brand.getId());
        response.setName(brand.getName());
        response.setSlug(brand.getSlug());
        response.setDescription(brand.getDescription());
        response.setCreatedAt(brand.getCreatedAt());
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
