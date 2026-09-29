package np.com.thapanarayan.ecommerce.dto.request;

import jakarta.validation.constraints.NotNull;
import np.com.thapanarayan.ecommerce.domain.enums.OrderStatus;
import np.com.thapanarayan.ecommerce.dto.ModelBase;

public class UpdateOrderStatusRequest extends ModelBase {

    @NotNull(message = "Order status is required")
    private OrderStatus status;

    public UpdateOrderStatusRequest() {
    }

    public UpdateOrderStatusRequest(OrderStatus status) {
        this.status = status;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
