package np.com.thapanarayan.ecommerce.domain.entity;

import java.util.Objects;
import java.util.UUID;
import np.com.thapanarayan.ecommerce.dto.ModelBase;

public class OrderItemId extends ModelBase {

    private UUID id;
    private UUID userId;

    public OrderItemId() {
    }

    public OrderItemId(UUID id, UUID userId) {
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
        OrderItemId that = (OrderItemId) o;
        return Objects.equals(id, that.id) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId);
    }
}
