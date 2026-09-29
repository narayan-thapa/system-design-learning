package np.com.thapanarayan.ecommerce.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import np.com.thapanarayan.ecommerce.domain.entity.Order;
import np.com.thapanarayan.ecommerce.domain.entity.Payment;
import np.com.thapanarayan.ecommerce.domain.enums.OrderStatus;
import np.com.thapanarayan.ecommerce.domain.enums.PaymentMethod;
import np.com.thapanarayan.ecommerce.domain.enums.PaymentStatus;
import np.com.thapanarayan.ecommerce.dto.request.PaymentCollectionRequest;
import np.com.thapanarayan.ecommerce.dto.response.PaymentResponse;
import np.com.thapanarayan.ecommerce.exception.BadRequestException;
import np.com.thapanarayan.ecommerce.exception.PaymentValidationException;
import np.com.thapanarayan.ecommerce.exception.ResourceNotFoundException;
import np.com.thapanarayan.ecommerce.repository.OrderRepository;
import np.com.thapanarayan.ecommerce.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID paymentId, UUID userId) {
        Payment payment = paymentRepository.findByIdAndUserId(paymentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId + " for user: " + userId));
        return PaymentResponse.fromEntity(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(UUID orderId, UUID userId) {
        Payment payment = paymentRepository.findByOrderIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order id: " + orderId + " and user: " + userId));
        return PaymentResponse.fromEntity(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUserId(UUID userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public PaymentResponse collectCashPayment(UUID paymentId, UUID userId, PaymentCollectionRequest request) {
        Payment payment = paymentRepository.findByIdAndUserId(paymentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId + " for user: " + userId));

        if (payment.getPaymentMethod() != PaymentMethod.CASH_ON_DELIVERY && payment.getPaymentMethod() != PaymentMethod.CASH) {
            throw new PaymentValidationException("Invalid operation: Only cash payments can be marked as collected via cash collection.");
        }

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new BadRequestException("Payment has already been marked as collected.");
        }

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCollectedAt(Instant.now());
        if (request != null && request.getTransactionReference() != null) {
            payment.setTransactionReference(request.getTransactionReference());
        }
        if (request != null && request.getNotes() != null) {
            payment.setNotes(request.getNotes());
        }
        payment.setUpdatedAt(Instant.now());

        Payment updatedPayment = paymentRepository.save(payment);

        // Update corresponding order status to DELIVERED upon cash collection
        orderRepository.findByIdAndUserId(payment.getOrderId(), userId).ifPresent(order -> {
            if (order.getStatus() != OrderStatus.CANCELLED && order.getStatus() != OrderStatus.DELIVERED) {
                order.setStatus(OrderStatus.DELIVERED);
                order.setUpdatedAt(Instant.now());
                orderRepository.save(order);
            }
        });

        return PaymentResponse.fromEntity(updatedPayment);
    }
}
