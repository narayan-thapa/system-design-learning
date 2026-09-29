package np.com.thapanarayan.ecommerce.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import np.com.thapanarayan.ecommerce.domain.entity.Order;
import np.com.thapanarayan.ecommerce.domain.entity.OrderItem;
import np.com.thapanarayan.ecommerce.domain.entity.Payment;
import np.com.thapanarayan.ecommerce.domain.enums.OrderStatus;
import np.com.thapanarayan.ecommerce.dto.response.OrderItemResponse;
import np.com.thapanarayan.ecommerce.dto.response.OrderResponse;
import np.com.thapanarayan.ecommerce.dto.response.PaymentResponse;
import np.com.thapanarayan.ecommerce.exception.ResourceNotFoundException;
import np.com.thapanarayan.ecommerce.repository.OrderItemRepository;
import np.com.thapanarayan.ecommerce.repository.OrderRepository;
import np.com.thapanarayan.ecommerce.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId, UUID userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId + " for user: " + userId));

        List<OrderItemResponse> items = orderItemRepository.findByOrderIdAndUserId(orderId, userId).stream()
                .map(OrderItemResponse::fromEntity)
                .collect(Collectors.toList());

        PaymentResponse paymentResponse = paymentRepository.findByOrderIdAndUserId(orderId, userId)
                .map(PaymentResponse::fromEntity)
                .orElse(null);

        return OrderResponse.fromEntity(order, items, paymentResponse);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(UUID userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return orders.stream().map(order -> {
            List<OrderItemResponse> items = orderItemRepository.findByOrderIdAndUserId(order.getId(), userId).stream()
                    .map(OrderItemResponse::fromEntity)
                    .collect(Collectors.toList());

            PaymentResponse payment = paymentRepository.findByOrderIdAndUserId(order.getId(), userId)
                    .map(PaymentResponse::fromEntity)
                    .orElse(null);

            return OrderResponse.fromEntity(order, items, payment);
        }).collect(Collectors.toList());
    }

    public OrderResponse updateOrderStatus(UUID orderId, UUID userId, OrderStatus newStatus) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId + " for user: " + userId));

        order.setStatus(newStatus);
        order.setUpdatedAt(Instant.now());
        Order updated = orderRepository.save(order);

        List<OrderItemResponse> items = orderItemRepository.findByOrderIdAndUserId(orderId, userId).stream()
                .map(OrderItemResponse::fromEntity)
                .collect(Collectors.toList());

        PaymentResponse payment = paymentRepository.findByOrderIdAndUserId(orderId, userId)
                .map(PaymentResponse::fromEntity)
                .orElse(null);

        return OrderResponse.fromEntity(updated, items, payment);
    }
}
