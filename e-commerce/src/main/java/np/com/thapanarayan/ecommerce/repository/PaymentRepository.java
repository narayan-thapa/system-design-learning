package np.com.thapanarayan.ecommerce.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import np.com.thapanarayan.ecommerce.domain.entity.Payment;
import np.com.thapanarayan.ecommerce.domain.entity.PaymentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, PaymentId> {
    Optional<Payment> findByIdAndUserId(UUID id, UUID userId);
    Optional<Payment> findByOrderIdAndUserId(UUID orderId, UUID userId);
    List<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
