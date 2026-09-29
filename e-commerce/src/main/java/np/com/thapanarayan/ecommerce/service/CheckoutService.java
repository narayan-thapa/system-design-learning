package np.com.thapanarayan.ecommerce.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import np.com.thapanarayan.ecommerce.domain.entity.Cart;
import np.com.thapanarayan.ecommerce.domain.entity.CartItem;
import np.com.thapanarayan.ecommerce.domain.entity.Order;
import np.com.thapanarayan.ecommerce.domain.entity.OrderItem;
import np.com.thapanarayan.ecommerce.domain.entity.Payment;
import np.com.thapanarayan.ecommerce.domain.entity.Product;
import np.com.thapanarayan.ecommerce.domain.enums.CartStatus;
import np.com.thapanarayan.ecommerce.domain.enums.OrderStatus;
import np.com.thapanarayan.ecommerce.domain.enums.PaymentMethod;
import np.com.thapanarayan.ecommerce.domain.enums.PaymentStatus;
import np.com.thapanarayan.ecommerce.domain.enums.ProductStatus;
import np.com.thapanarayan.ecommerce.dto.request.CheckoutRequest;
import np.com.thapanarayan.ecommerce.dto.response.CheckoutResponse;
import np.com.thapanarayan.ecommerce.dto.response.OrderItemResponse;
import np.com.thapanarayan.ecommerce.dto.response.OrderResponse;
import np.com.thapanarayan.ecommerce.dto.response.PaymentResponse;
import np.com.thapanarayan.ecommerce.exception.BadRequestException;
import np.com.thapanarayan.ecommerce.exception.InsufficientStockException;
import np.com.thapanarayan.ecommerce.exception.PaymentValidationException;
import np.com.thapanarayan.ecommerce.exception.ResourceNotFoundException;
import np.com.thapanarayan.ecommerce.repository.CartItemRepository;
import np.com.thapanarayan.ecommerce.repository.CartRepository;
import np.com.thapanarayan.ecommerce.repository.OrderItemRepository;
import np.com.thapanarayan.ecommerce.repository.OrderRepository;
import np.com.thapanarayan.ecommerce.repository.PaymentRepository;
import np.com.thapanarayan.ecommerce.repository.ProductRepository;
import np.com.thapanarayan.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CheckoutService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;

    public CheckoutService(UserRepository userRepository,
                           CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           ProductRepository productRepository,
                           OrderRepository orderRepository,
                           OrderItemRepository orderItemRepository,
                           PaymentRepository paymentRepository) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
    }

    public CheckoutResponse checkout(CheckoutRequest request) {
        UUID userId = request.getUserId();

        // 1. Validate User
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // 2. Validate Payment Method: Cash only requirement
        PaymentMethod paymentMethod = request.getPaymentMethod();
        if (paymentMethod == null) {
            paymentMethod = PaymentMethod.CASH_ON_DELIVERY;
        }
        if (!paymentMethod.isCashMethod()) {
            throw new PaymentValidationException("Only cash payments (CASH_ON_DELIVERY or CASH) are supported at this time. Provided: " + paymentMethod);
        }

        // 3. Retrieve user active cart and items
        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE).orElse(null);
        if (cart == null) {
            throw new BadRequestException("Cannot checkout an empty cart. Please add items before checking out.");
        }

        List<CartItem> cartItems = cartItemRepository.findByCartIdAndUserId(cart.getId(), userId);
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cannot checkout an empty cart. Please add items before checking out.");
        }

        // 4. Fetch and validate all products & stock
        Map<UUID, Product> products = productRepository.findAllById(
                cartItems.stream().map(CartItem::getProductId).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        for (CartItem item : cartItems) {
            Product product = products.get(item.getProductId());
            if (product == null) {
                throw new ResourceNotFoundException("Product with id " + item.getProductId() + " no longer exists");
            }
            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new BadRequestException("Product '" + product.getName() + "' is currently inactive and cannot be purchased");
            }
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product '" + product.getName() +
                        "'. Requested: " + item.getQuantity() + ", Available: " + product.getStockQuantity());
            }
        }

        // 5. Deduct product inventory
        for (CartItem item : cartItems) {
            Product product = products.get(item.getProductId());
            int remainingStock = product.getStockQuantity() - item.getQuantity();
            product.setStockQuantity(remainingStock);
            if (remainingStock == 0) {
                product.setStatus(ProductStatus.OUT_OF_STOCK);
            }
            product.setUpdatedAt(Instant.now());
            productRepository.save(product);
        }

        // 6. Create Order and Order Items (co-located on user_id)
        UUID orderId = UUID.randomUUID();
        String orderNumber = "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        List<OrderItemResponse> itemResponses = new ArrayList<>();

        for (CartItem item : cartItems) {
            Product product = products.get(item.getProductId());
            BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = new OrderItem(
                    UUID.randomUUID(),
                    userId,
                    orderId,
                    product.getId(),
                    product.getName(),
                    item.getUnitPrice(),
                    item.getQuantity(),
                    subtotal
            );
            orderItems.add(orderItem);
            itemResponses.add(OrderItemResponse.fromEntity(orderItem));
        }

        Order order = new Order(
                orderId,
                userId,
                orderNumber,
                totalAmount,
                OrderStatus.PLACED,
                request.getShippingAddress(),
                request.getCustomerNotes()
        );

        Order savedOrder = orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

        // 7. Create Payment (Cash only, PENDING collection on delivery, co-located on user_id)
        Payment payment = new Payment(
                UUID.randomUUID(),
                userId,
                savedOrder.getId(),
                totalAmount,
                paymentMethod,
                PaymentStatus.PENDING,
                "COD-" + orderNumber,
                "Cash on delivery payment pending collection upon receipt of shipment."
        );
        Payment savedPayment = paymentRepository.save(payment);
        PaymentResponse paymentResponse = PaymentResponse.fromEntity(savedPayment);

        // 8. Mark Cart as CHECKED_OUT
        cart.setStatus(CartStatus.CHECKED_OUT);
        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);

        OrderResponse orderResponse = OrderResponse.fromEntity(savedOrder, itemResponses, paymentResponse);

        return new CheckoutResponse(
                orderResponse,
                paymentResponse,
                "Checkout completed successfully with cash on delivery payment method. Order placed."
        );
    }
}
