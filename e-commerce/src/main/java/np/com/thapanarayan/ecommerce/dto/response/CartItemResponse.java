package np.com.thapanarayan.ecommerce.dto.response;

import java.math.BigDecimal;
import java.util.UUID;
import np.com.thapanarayan.ecommerce.domain.entity.CartItem;
import np.com.thapanarayan.ecommerce.dto.ModelBase;

public class CartItemResponse extends ModelBase {

    private UUID id;
    private UUID cartId;
    private UUID productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public CartItemResponse() {
    }

    public static CartItemResponse fromEntity(CartItem item, String productName) {
        CartItemResponse response = new CartItemResponse();
        response.setId(item.getId());
        response.setCartId(item.getCartId());
        response.setProductId(item.getProductId());
        response.setProductName(productName);
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setSubtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        return response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCartId() {
        return cartId;
    }

    public void setCartId(UUID cartId) {
        this.cartId = cartId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
