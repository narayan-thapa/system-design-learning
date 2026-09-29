package np.com.thapanarayan.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import np.com.thapanarayan.ecommerce.domain.enums.PaymentMethod;

public class CheckoutRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    private String customerNotes;

    private PaymentMethod paymentMethod = PaymentMethod.CASH_ON_DELIVERY;

    public CheckoutRequest() {
    }

    public CheckoutRequest(UUID userId, String shippingAddress, String customerNotes, PaymentMethod paymentMethod) {
        this.userId = userId;
        this.shippingAddress = shippingAddress;
        this.customerNotes = customerNotes;
        this.paymentMethod = paymentMethod != null ? paymentMethod : PaymentMethod.CASH_ON_DELIVERY;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getCustomerNotes() {
        return customerNotes;
    }

    public void setCustomerNotes(String customerNotes) {
        this.customerNotes = customerNotes;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
