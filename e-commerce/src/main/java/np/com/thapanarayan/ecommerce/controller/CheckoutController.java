package np.com.thapanarayan.ecommerce.controller;

import jakarta.validation.Valid;
import np.com.thapanarayan.ecommerce.dto.GenericResponse;
import np.com.thapanarayan.ecommerce.dto.request.CheckoutRequest;
import np.com.thapanarayan.ecommerce.dto.response.CheckoutResponse;
import np.com.thapanarayan.ecommerce.service.CheckoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping
    public ResponseEntity<GenericResponse<CheckoutResponse>> checkout(@Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(GenericResponse.success("Checkout processed successfully", checkoutService.checkout(request)));
    }
}
