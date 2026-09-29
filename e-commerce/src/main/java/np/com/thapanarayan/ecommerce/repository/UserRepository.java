package np.com.thapanarayan.ecommerce.repository;

import java.util.Optional;
import java.util.UUID;
import np.com.thapanarayan.ecommerce.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
