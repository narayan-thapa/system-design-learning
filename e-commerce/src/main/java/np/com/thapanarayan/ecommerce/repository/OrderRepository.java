package np.com.thapanarayan.ecommerce.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import np.com.thapanarayan.ecommerce.domain.entity.Order;
import np.com.thapanarayan.ecommerce.domain.entity.OrderId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, OrderId> {
    Optional<Order> findByIdAndUserId(UUID id, UUID userId);
    List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<Order> findByOrderNumber(String orderNumber);
}
