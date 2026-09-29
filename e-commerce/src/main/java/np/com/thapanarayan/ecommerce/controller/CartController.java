package np.com.thapanarayan.ecommerce.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import np.com.thapanarayan.ecommerce.dto.GenericResponse;
import np.com.thapanarayan.ecommerce.dto.request.AddToCartRequest;
import np.com.thapanarayan.ecommerce.dto.request.UpdateCartItemRequest;
import np.com.thapanarayan.ecommerce.dto.response.CartResponse;
import np.com.thapanarayan.ecommerce.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<GenericResponse<CartResponse>> getCart(@PathVariable UUID userId) {
        return ResponseEntity.ok(GenericResponse.success(cartService.getOrCreateActiveCart(userId)));
    }

    @PostMapping("/users/{userId}/items")
    public ResponseEntity<GenericResponse<CartResponse>> addItemToCart(@PathVariable UUID userId,
                                                                      @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(GenericResponse.success("Item added to cart successfully", cartService.addItemToCart(userId, request)));
    }

    @PutMapping("/users/{userId}/items/{itemId}")
    public ResponseEntity<GenericResponse<CartResponse>> updateItemQuantity(@PathVariable UUID userId,
                                                                           @PathVariable UUID itemId,
                                                                           @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(GenericResponse.success("Cart item quantity updated successfully", cartService.updateItemQuantity(userId, itemId, request)));
    }

    @DeleteMapping("/users/{userId}/items/{itemId}")
    public ResponseEntity<GenericResponse<CartResponse>> removeItem(@PathVariable UUID userId,
                                                                   @PathVariable UUID itemId) {
        return ResponseEntity.ok(GenericResponse.success("Cart item removed successfully", cartService.removeItem(userId, itemId)));
    }

    @DeleteMapping("/users/{userId}/clear")
    public ResponseEntity<GenericResponse<Void>> clearCart(@PathVariable UUID userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(GenericResponse.success("Cart cleared successfully", null));
    }
}
