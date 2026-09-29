package np.com.thapanarayan.ecommerce.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import np.com.thapanarayan.ecommerce.domain.entity.Cart;
import np.com.thapanarayan.ecommerce.domain.entity.CartItem;
import np.com.thapanarayan.ecommerce.domain.entity.Product;
import np.com.thapanarayan.ecommerce.domain.enums.CartStatus;
import np.com.thapanarayan.ecommerce.domain.enums.ProductStatus;
import np.com.thapanarayan.ecommerce.dto.request.AddToCartRequest;
import np.com.thapanarayan.ecommerce.dto.request.UpdateCartItemRequest;
import np.com.thapanarayan.ecommerce.dto.response.CartItemResponse;
import np.com.thapanarayan.ecommerce.dto.response.CartResponse;
import np.com.thapanarayan.ecommerce.exception.BadRequestException;
import np.com.thapanarayan.ecommerce.exception.InsufficientStockException;
import np.com.thapanarayan.ecommerce.exception.ResourceNotFoundException;
import np.com.thapanarayan.ecommerce.repository.CartItemRepository;
import np.com.thapanarayan.ecommerce.repository.CartRepository;
import np.com.thapanarayan.ecommerce.repository.ProductRepository;
import np.com.thapanarayan.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public CartResponse getOrCreateActiveCart(UUID userId) {
        validateUser(userId);
        Cart cart = getActiveCartEntity(userId);
        return buildCartResponse(cart);
    }

    public CartResponse addItemToCart(UUID userId, AddToCartRequest request) {
        validateUser(userId);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("Product is not currently available for purchase");
        }

        Cart cart = getActiveCartEntity(userId);

        Optional<CartItem> existingItemOpt = cartItemRepository
                .findByCartIdAndUserIdAndProductId(cart.getId(), userId, product.getId());

        int targetQuantity = request.getQuantity();
        if (existingItemOpt.isPresent()) {
            targetQuantity += existingItemOpt.get().getQuantity();
        }

        if (product.getStockQuantity() < targetQuantity) {
            throw new InsufficientStockException("Requested quantity " + targetQuantity +
                    " exceeds available stock (" + product.getStockQuantity() + ") for product: " + product.getName());
        }

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(targetQuantity);
            existingItem.setUnitPrice(product.getPrice());
            existingItem.setUpdatedAt(Instant.now());
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem(
                    UUID.randomUUID(),
                    userId,
                    cart.getId(),
                    product.getId(),
                    request.getQuantity(),
                    product.getPrice()
            );
            cartItemRepository.save(newItem);
        }

        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);

        return buildCartResponse(cart);
    }

    public CartResponse updateItemQuantity(UUID userId, UUID itemId, UpdateCartItemRequest request) {
        validateUser(userId);
        Cart cart = getActiveCartEntity(userId);

        CartItem item = cartItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        if (!item.getCartId().equals(cart.getId())) {
            throw new BadRequestException("Item does not belong to active cart");
        }

        if (request.getQuantity() <= 0) {
            cartItemRepository.delete(item);
        } else {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + item.getProductId()));

            if (product.getStockQuantity() < request.getQuantity()) {
                throw new InsufficientStockException("Requested quantity " + request.getQuantity() +
                        " exceeds available stock (" + product.getStockQuantity() + ") for product: " + product.getName());
            }

            item.setQuantity(request.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setUpdatedAt(Instant.now());
            cartItemRepository.save(item);
        }

        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);

        return buildCartResponse(cart);
    }

    public CartResponse removeItem(UUID userId, UUID itemId) {
        validateUser(userId);
        Cart cart = getActiveCartEntity(userId);

        CartItem item = cartItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        cartItemRepository.delete(item);
        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);

        return buildCartResponse(cart);
    }

    public void clearCart(UUID userId) {
        validateUser(userId);
        Cart cart = getActiveCartEntity(userId);
        cartItemRepository.deleteByCartIdAndUserId(cart.getId(), userId);
        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);
    }

    public Cart getActiveCartEntity(UUID userId) {
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart newCart = new Cart(UUID.randomUUID(), userId, CartStatus.ACTIVE);
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse buildCartResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartIdAndUserId(cart.getId(), cart.getUserId());

        Map<UUID, String> productNames = productRepository.findAllById(
                items.stream().map(CartItem::getProductId).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(Product::getId, Product::getName));

        List<CartItemResponse> itemResponses = items.stream()
                .map(item -> CartItemResponse.fromEntity(item, productNames.getOrDefault(item.getProductId(), "Unknown")))
                .collect(Collectors.toList());

        return CartResponse.fromEntity(cart, itemResponses);
    }

    private void validateUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }
}
