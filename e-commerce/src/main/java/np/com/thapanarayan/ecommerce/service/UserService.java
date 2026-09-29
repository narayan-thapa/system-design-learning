package np.com.thapanarayan.ecommerce.service;

import java.util.UUID;
import np.com.thapanarayan.ecommerce.domain.entity.User;
import np.com.thapanarayan.ecommerce.domain.enums.UserRole;
import np.com.thapanarayan.ecommerce.dto.request.UserRegisterRequest;
import np.com.thapanarayan.ecommerce.dto.request.UserUpdateRequest;
import np.com.thapanarayan.ecommerce.dto.response.UserResponse;
import np.com.thapanarayan.ecommerce.exception.BadRequestException;
import np.com.thapanarayan.ecommerce.exception.ResourceNotFoundException;
import np.com.thapanarayan.ecommerce.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email " + request.getEmail() + " is already registered");
        }

        // Basic password hashing placeholder (e.g. SHA-256 or BCrypt)
        String passwordHash = "HASH_" + Integer.toHexString(request.getPassword().hashCode());

        User user = new User(
                UUID.randomUUID(),
                request.getEmail().toLowerCase().trim(),
                passwordHash,
                request.getFirstName().trim(),
                request.getLastName().trim(),
                request.getPhone(),
                request.getShippingAddress(),
                request.getRole() != null ? request.getRole() : UserRole.CUSTOMER
        );

        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserResponse.fromEntity(user);
    }

    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setPhone(request.getPhone());
        user.setShippingAddress(request.getShippingAddress());
        user.setUpdatedAt(java.time.Instant.now());

        User updated = userRepository.save(user);
        return UserResponse.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::fromEntity);
    }
}
