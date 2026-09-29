package np.com.thapanarayan.ecommerce.repository;

import java.util.List;
import java.util.UUID;
import np.com.thapanarayan.ecommerce.domain.entity.OrderItem;
import np.com.thapanarayan.ecommerce.domain.entity.OrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
    List<OrderItem> findByOrderIdAndUserId(UUID orderId, UUID userId);
}
