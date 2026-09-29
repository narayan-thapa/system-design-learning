package np.com.thapanarayan.ecommerce.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GenericResponse<T> extends ModelBase {
    private Boolean success;
    private String message;
    private T data;
    private List<ErrorDetail> errors;

    public GenericResponse() {
    }

    public GenericResponse(Boolean success, String message, T data, List<ErrorDetail> errors) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }

    public GenericResponse(Boolean success, String message, T data) {
        this(success, message, data, null);
    }

    public GenericResponse(Boolean success, String message, List<ErrorDetail> errors) {
        this(success, message, null, errors);
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<ErrorDetail> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorDetail> errors) {
        this.errors = errors;
    }

    public void addError(ErrorDetail errorDetail) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(errorDetail);
    }

    public void addError(String message) {
        addError(new ErrorDetail(null, message, null, null));
    }

    public void addError(String error, String message) {
        addError(new ErrorDetail(null, message, error, null));
    }

    public static <T> GenericResponse<T> success(T data) {
        return new GenericResponse<>(true, "Operation successful", data, null);
    }

    public static <T> GenericResponse<T> success(String message, T data) {
        return new GenericResponse<>(true, message, data, null);
    }

    public static <T> GenericResponse<T> error(String message) {
        return new GenericResponse<>(false, message, null, Collections.singletonList(new ErrorDetail(null, message, null, null)));
    }

    public static <T> GenericResponse<T> error(String message, String error) {
        return new GenericResponse<>(false, message, null, Collections.singletonList(new ErrorDetail(null, message, error, null)));
    }

    public static <T> GenericResponse<T> error(String message, List<ErrorDetail> errors) {
        return new GenericResponse<>(false, message, null, errors);
    }

    public static <T> GenericResponse<T> error(String message, ErrorDetail errorDetail) {
        return new GenericResponse<>(false, message, null, Collections.singletonList(errorDetail));
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private Boolean success;
        private String message;
        private T data;
        private List<ErrorDetail> errors;

        public Builder<T> success(Boolean success) {
            this.success = success;
            return this;
        }

        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        public Builder<T> errors(List<ErrorDetail> errors) {
            this.errors = errors;
            return this;
        }

        public Builder<T> error(ErrorDetail errorDetail) {
            if (this.errors == null) {
                this.errors = new ArrayList<>();
            }
            this.errors.add(errorDetail);
            return this;
        }

        public Builder<T> error(String message) {
            return error(new ErrorDetail(null, message, null, null));
        }

        public Builder<T> error(String error, String message) {
            return error(new ErrorDetail(null, message, error, null));
        }

        public GenericResponse<T> build() {
            return new GenericResponse<>(success, message, data, errors);
        }
    }
}
