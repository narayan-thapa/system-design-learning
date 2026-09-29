package np.com.thapanarayan.ecommerce.exception;

import java.util.List;
import java.util.stream.Collectors;
import np.com.thapanarayan.ecommerce.dto.ErrorDetail;
import np.com.thapanarayan.ecommerce.dto.GenericResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GenericResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorDetail errorDetail = new ErrorDetail(null, ex.getMessage(), HttpStatus.NOT_FOUND.getReasonPhrase(), null);
        return new ResponseEntity<>(
                GenericResponse.error(ex.getMessage(), List.of(errorDetail)),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<GenericResponse<Void>> handleBadRequest(BadRequestException ex) {
        ErrorDetail errorDetail = new ErrorDetail(null, ex.getMessage(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null);
        return new ResponseEntity<>(
                GenericResponse.error(ex.getMessage(), List.of(errorDetail)),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<GenericResponse<Void>> handleInsufficientStock(InsufficientStockException ex) {
        ErrorDetail errorDetail = new ErrorDetail(null, ex.getMessage(), "Insufficient Stock", null);
        return new ResponseEntity<>(
                GenericResponse.error(ex.getMessage(), List.of(errorDetail)),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(PaymentValidationException.class)
    public ResponseEntity<GenericResponse<Void>> handlePaymentValidation(PaymentValidationException ex) {
        ErrorDetail errorDetail = new ErrorDetail("paymentMethod", ex.getMessage(), "Payment Validation Error", null);
        return new ResponseEntity<>(
                GenericResponse.error(ex.getMessage(), List.of(errorDetail)),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<ErrorDetail> errorDetails = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ErrorDetail(
                        fieldError.getField(),
                        fieldError.getDefaultMessage(),
                        fieldError.getCode(),
                        fieldError.getRejectedValue() != null ? String.valueOf(fieldError.getRejectedValue()) : null
                ))
                .collect(Collectors.toList());

        return new ResponseEntity<>(
                GenericResponse.error("Validation Failed", errorDetails),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse<Void>> handleGeneralException(Exception ex) {
        ErrorDetail errorDetail = new ErrorDetail(
                null,
                ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                null
        );
        return new ResponseEntity<>(
                GenericResponse.error(
                        ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred",
                        List.of(errorDetail)
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
