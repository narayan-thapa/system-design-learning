package np.com.thapanarayan.ecommerce.domain.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class PaymentId implements Serializable {

    private UUID id;
    private UUID userId;

    public PaymentId() {
    }

    public PaymentId(UUID id, UUID userId) {
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
        PaymentId paymentId = (PaymentId) o;
        return Objects.equals(id, paymentId.id) && Objects.equals(userId, paymentId.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId);
    }
}
