package np.com.thapanarayan.ecommerce.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import np.com.thapanarayan.ecommerce.dto.GenericResponse;
import np.com.thapanarayan.ecommerce.dto.request.UserRegisterRequest;
import np.com.thapanarayan.ecommerce.dto.request.UserUpdateRequest;
import np.com.thapanarayan.ecommerce.dto.response.UserResponse;
import np.com.thapanarayan.ecommerce.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<GenericResponse<UserResponse>> register(@Valid @RequestBody UserRegisterRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + response.getId()))
                .body(GenericResponse.success("User registered successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(GenericResponse.success(userService.getUserById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse<UserResponse>> updateUser(@PathVariable UUID id,
                                                                   @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(GenericResponse.success("User updated successfully", userService.updateUser(id, request)));
    }

    @GetMapping
    public ResponseEntity<GenericResponse<Page<UserResponse>>> getAllUsers(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(GenericResponse.success(userService.getAllUsers(pageable)));
    }
}
