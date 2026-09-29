package np.com.thapanarayan.ecommerce.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import np.com.thapanarayan.ecommerce.domain.entity.CartItem;
import np.com.thapanarayan.ecommerce.domain.entity.CartItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {
    List<CartItem> findByCartIdAndUserId(UUID cartId, UUID userId);
    Optional<CartItem> findByIdAndUserId(UUID id, UUID userId);
    Optional<CartItem> findByCartIdAndUserIdAndProductId(UUID cartId, UUID userId, UUID productId);
    void deleteByCartIdAndUserId(UUID cartId, UUID userId);
}
