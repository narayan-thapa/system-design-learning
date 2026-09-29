package np.com.thapanarayan.ecommerce.domain.entity;

import java.util.Objects;
import java.util.UUID;
import np.com.thapanarayan.ecommerce.dto.ModelBase;

public class OrderId extends ModelBase {

    private UUID id;
    private UUID userId;

    public OrderId() {
    }

    public OrderId(UUID id, UUID userId) {
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
        OrderId orderId = (OrderId) o;
        return Objects.equals(id, orderId.id) && Objects.equals(userId, orderId.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId);
    }
}
