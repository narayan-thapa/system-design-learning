package np.com.thapanarayan.ecommerce.dto.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import np.com.thapanarayan.ecommerce.domain.entity.Cart;
import np.com.thapanarayan.ecommerce.domain.enums.CartStatus;
import np.com.thapanarayan.ecommerce.dto.ModelBase;

public class CartResponse extends ModelBase {

    private UUID id;
    private UUID userId;
    private CartStatus status;
    private List<CartItemResponse> items = new ArrayList<>();
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private Integer totalItemCount = 0;

    public CartResponse() {
    }

    public static CartResponse fromEntity(Cart cart, List<CartItemResponse> items) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setUserId(cart.getUserId());
        response.setStatus(cart.getStatus());
        response.setItems(items != null ? items : new ArrayList<>());

        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        if (items != null) {
            for (CartItemResponse item : items) {
                total = total.add(item.getSubtotal());
                count += item.getQuantity();
            }
        }
        response.setTotalAmount(total);
        response.setTotalItemCount(count);
        return response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public CartStatus getStatus() {
        return status;
    }

    public void setStatus(CartStatus status) {
        this.status = status;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getTotalItemCount() {
        return totalItemCount;
    }

    public void setTotalItemCount(Integer totalItemCount) {
        this.totalItemCount = totalItemCount;
    }
}
