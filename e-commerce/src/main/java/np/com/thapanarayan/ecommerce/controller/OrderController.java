package np.com.thapanarayan.ecommerce.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import np.com.thapanarayan.ecommerce.dto.GenericResponse;
import np.com.thapanarayan.ecommerce.dto.request.UpdateOrderStatusRequest;
import np.com.thapanarayan.ecommerce.dto.response.OrderResponse;
import np.com.thapanarayan.ecommerce.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<GenericResponse<List<OrderResponse>>> getOrdersByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(GenericResponse.success(orderService.getOrdersByUserId(userId)));
    }

    @GetMapping("/{orderId}/users/{userId}")
    public ResponseEntity<GenericResponse<OrderResponse>> getOrderById(@PathVariable UUID orderId,
                                                                      @PathVariable UUID userId) {
        return ResponseEntity.ok(GenericResponse.success(orderService.getOrderById(orderId, userId)));
    }

    @PutMapping("/{orderId}/users/{userId}/status")
    public ResponseEntity<GenericResponse<OrderResponse>> updateOrderStatus(@PathVariable UUID orderId,
                                                                           @PathVariable UUID userId,
                                                                           @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(GenericResponse.success("Order status updated successfully",
                orderService.updateOrderStatus(orderId, userId, request.getStatus())));
    }
}
