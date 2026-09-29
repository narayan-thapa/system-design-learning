package np.com.thapanarayan.ecommerce.domain.entity;

import java.util.Objects;
import java.util.UUID;
import np.com.thapanarayan.ecommerce.dto.ModelBase;

public class CartId extends ModelBase {

    private UUID id;
    private UUID userId;

    public CartId() {
    }

    public CartId(UUID id, UUID userId) {
        this.id = id;
        this.userId = userId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartId cartId = (CartId) o;
        return Objects.equals(id, cartId.id) && Objects.equals(userId, cartId.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId);
    }
}
