package np.com.thapanarayan.ecommerce.dto.response;

public class CheckoutResponse {

    private OrderResponse order;
    private PaymentResponse payment;
    private String message;

    public CheckoutResponse() {
    }

    public CheckoutResponse(OrderResponse order, PaymentResponse payment, String message) {
        this.order = order;
        this.payment = payment;
        this.message = message;
    }

    public OrderResponse getOrder() {
        return order;
    }

    public void setOrder(OrderResponse order) {
        this.order = order;
    }

    public PaymentResponse getPayment() {
        return payment;
    }

    public void setPayment(PaymentResponse payment) {
        this.payment = payment;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
