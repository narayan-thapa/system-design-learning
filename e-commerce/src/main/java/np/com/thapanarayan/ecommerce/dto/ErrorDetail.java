package np.com.thapanarayan.ecommerce.dto;

public class ErrorDetail extends ModelBase {
    private String field;
    private String message;
    private String error;
    private String rejectedValue;

    public ErrorDetail() {
    }

    public ErrorDetail(String message) {
        this.message = message;
    }

    public ErrorDetail(String error, String message) {
        this.error = error;
        this.message = message;
    }

    public ErrorDetail(String field, String message, String error, String rejectedValue) {
        this.field = field;
        this.message = message;
        this.error = error;
        this.rejectedValue = rejectedValue;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getRejectedValue() {
        return rejectedValue;
    }

    public void setRejectedValue(String rejectedValue) {
        this.rejectedValue = rejectedValue;
    }
}
