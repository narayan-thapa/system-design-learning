package np.com.thapanarayan.ecommerce.controller;

import java.util.List;
import java.util.UUID;
import np.com.thapanarayan.ecommerce.dto.GenericResponse;
import np.com.thapanarayan.ecommerce.dto.request.PaymentCollectionRequest;
import np.com.thapanarayan.ecommerce.dto.response.PaymentResponse;
import np.com.thapanarayan.ecommerce.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/{paymentId}/users/{userId}")
    public ResponseEntity<GenericResponse<PaymentResponse>> getPaymentById(@PathVariable UUID paymentId,
                                                                          @PathVariable UUID userId) {
        return ResponseEntity.ok(GenericResponse.success(paymentService.getPaymentById(paymentId, userId)));
    }

    @GetMapping("/orders/{orderId}/users/{userId}")
    public ResponseEntity<GenericResponse<PaymentResponse>> getPaymentByOrderId(@PathVariable UUID orderId,
                                                                              @PathVariable UUID userId) {
        return ResponseEntity.ok(GenericResponse.success(paymentService.getPaymentByOrderId(orderId, userId)));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<GenericResponse<List<PaymentResponse>>> getPaymentsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(GenericResponse.success(paymentService.getPaymentsByUserId(userId)));
    }

    @PostMapping("/{paymentId}/users/{userId}/collect-cash")
    public ResponseEntity<GenericResponse<PaymentResponse>> collectCashPayment(@PathVariable UUID paymentId,
                                                                              @PathVariable UUID userId,
                                                                              @RequestBody(required = false) PaymentCollectionRequest request) {
        return ResponseEntity.ok(GenericResponse.success("Cash payment collected successfully",
                paymentService.collectCashPayment(paymentId, userId, request)));
    }
}
