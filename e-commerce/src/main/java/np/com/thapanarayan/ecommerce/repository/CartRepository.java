package np.com.thapanarayan.ecommerce.repository;

import java.util.Optional;
import java.util.UUID;
import np.com.thapanarayan.ecommerce.domain.entity.Cart;
import np.com.thapanarayan.ecommerce.domain.entity.CartId;
import np.com.thapanarayan.ecommerce.domain.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, CartId> {
    Optional<Cart> findByIdAndUserId(UUID id, UUID userId);
    Optional<Cart> findByUserIdAndStatus(UUID userId, CartStatus status);
}
