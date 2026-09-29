package np.com.thapanarayan.ecommerce.dto.request;

import np.com.thapanarayan.ecommerce.dto.ModelBase;

public class PaymentCollectionRequest extends ModelBase {

    private String transactionReference;
    private String notes;

    public PaymentCollectionRequest() {
    }

    public PaymentCollectionRequest(String transactionReference, String notes) {
        this.transactionReference = transactionReference;
        this.notes = notes;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
