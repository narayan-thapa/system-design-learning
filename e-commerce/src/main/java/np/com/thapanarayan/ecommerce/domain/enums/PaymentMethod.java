package np.com.thapanarayan.ecommerce.domain.enums;

public enum PaymentMethod {
    CASH_ON_DELIVERY,
    CASH,
    CREDIT_CARD,
    DEBIT_CARD,
    DIGITAL_WALLET;

    public boolean isCashMethod() {
        return this == CASH_ON_DELIVERY || this == CASH;
    }

    public static boolean isCashMethod(String method) {
        if (method == null) return false;
        try {
            PaymentMethod pm = PaymentMethod.valueOf(method.toUpperCase());
            return pm.isCashMethod();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
