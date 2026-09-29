package np.com.thapanarayan.ecommerce;

import java.math.BigDecimal;
import java.util.UUID;
import np.com.thapanarayan.ecommerce.domain.enums.OrderStatus;
import np.com.thapanarayan.ecommerce.domain.enums.PaymentMethod;
import np.com.thapanarayan.ecommerce.domain.enums.PaymentStatus;
import np.com.thapanarayan.ecommerce.domain.enums.ProductStatus;
import np.com.thapanarayan.ecommerce.domain.enums.UserRole;
import np.com.thapanarayan.ecommerce.dto.request.AddToCartRequest;
import np.com.thapanarayan.ecommerce.dto.request.BrandRequest;
import np.com.thapanarayan.ecommerce.dto.request.CategoryRequest;
import np.com.thapanarayan.ecommerce.dto.request.CheckoutRequest;
import np.com.thapanarayan.ecommerce.dto.request.PaymentCollectionRequest;
import np.com.thapanarayan.ecommerce.dto.request.ProductRequest;
import np.com.thapanarayan.ecommerce.dto.request.UpdateCartItemRequest;
import np.com.thapanarayan.ecommerce.dto.request.UpdateOrderStatusRequest;
import np.com.thapanarayan.ecommerce.dto.request.UserRegisterRequest;
import np.com.thapanarayan.ecommerce.dto.response.BrandResponse;
import np.com.thapanarayan.ecommerce.dto.response.CartResponse;
import np.com.thapanarayan.ecommerce.dto.response.CategoryResponse;
import np.com.thapanarayan.ecommerce.dto.response.CheckoutResponse;
import np.com.thapanarayan.ecommerce.dto.response.ProductResponse;
import np.com.thapanarayan.ecommerce.dto.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EcommerceApiIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @Order(1)
    void shouldRegisterAndRetrieveUser() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("john.doe@example.com");
        request.setPassword("securePassword123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhone("+1234567890");
        request.setShippingAddress("123 Main Street, New York, NY");
        request.setRole(UserRole.CUSTOMER);

        MvcResult result = mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        UserResponse user = objectMapper.treeToValue(root.get("data"), UserResponse.class);
        assertThat(user.getId()).isNotNull();

        // Retrieve user
        mockMvc.perform(get("/api/v1/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"));
    }

    @Test
    @Order(2)
    void shouldManageCategoryAndBrand() throws Exception {
        // Category
        CategoryRequest catRequest = new CategoryRequest();
        catRequest.setName("Electronics");
        catRequest.setDescription("Electronic devices and gadgets");

        MvcResult catResult = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(catRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Electronics"))
                .andExpect(jsonPath("$.data.slug").value("electronics"))
                .andReturn();

        JsonNode catRoot = objectMapper.readTree(catResult.getResponse().getContentAsString());
        CategoryResponse category = objectMapper.treeToValue(catRoot.get("data"), CategoryResponse.class);

        // Brand
        BrandRequest brandRequest = new BrandRequest();
        brandRequest.setName("Sony");
        brandRequest.setDescription("Sony Electronics Corporation");

        MvcResult brandResult = mockMvc.perform(post("/api/v1/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Sony"))
                .andExpect(jsonPath("$.data.slug").value("sony"))
                .andReturn();

        JsonNode brandRoot = objectMapper.readTree(brandResult.getResponse().getContentAsString());
        BrandResponse brand = objectMapper.treeToValue(brandRoot.get("data"), BrandResponse.class);

        assertThat(category.getId()).isNotNull();
        assertThat(brand.getId()).isNotNull();
    }

    @Test
    @Order(3)
    void shouldExecuteFullEcommerceOrderFlowWithCashPayment() throws Exception {
        // 1. Register User
        UserRegisterRequest userReq = new UserRegisterRequest();
        userReq.setEmail("alice@example.com");
        userReq.setPassword("alicePassword456");
        userReq.setFirstName("Alice");
        userReq.setLastName("Smith");
        userReq.setShippingAddress("456 Market St, San Francisco, CA");
        MvcResult userResult = mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReq)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode userRoot = objectMapper.readTree(userResult.getResponse().getContentAsString());
        UserResponse user = objectMapper.treeToValue(userRoot.get("data"), UserResponse.class);

        // 2. Create Category and Brand
        CategoryRequest catReq = new CategoryRequest();
        catReq.setName("Computers");
        catReq.setDescription("Laptops and accessories");
        MvcResult catResult = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(catReq)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode catRoot = objectMapper.readTree(catResult.getResponse().getContentAsString());
        CategoryResponse category = objectMapper.treeToValue(catRoot.get("data"), CategoryResponse.class);

        BrandRequest brandReq = new BrandRequest();
        brandReq.setName("Apple");
        brandReq.setDescription("Apple Inc.");
        MvcResult brandResult = mockMvc.perform(post("/api/v1/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandReq)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode brandRoot = objectMapper.readTree(brandResult.getResponse().getContentAsString());
        BrandResponse brand = objectMapper.treeToValue(brandRoot.get("data"), BrandResponse.class);

        // 3. Create Product
        ProductRequest prodReq = new ProductRequest();
        prodReq.setName("MacBook Pro 14");
        prodReq.setDescription("Apple M3 Pro Chip, 18GB Unified Memory");
        prodReq.setPrice(new BigDecimal("1999.00"));
        prodReq.setStockQuantity(10);
        prodReq.setCategoryId(category.getId());
        prodReq.setBrandId(brand.getId());
        prodReq.setStatus(ProductStatus.ACTIVE);

        MvcResult prodResult = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prodReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("MacBook Pro 14"))
                .andExpect(jsonPath("$.data.stockQuantity").value(10))
                .andReturn();
        JsonNode prodRoot = objectMapper.readTree(prodResult.getResponse().getContentAsString());
        ProductResponse product = objectMapper.treeToValue(prodRoot.get("data"), ProductResponse.class);

        // 4. Add to Cart
        AddToCartRequest cartReq = new AddToCartRequest(product.getId(), 2);
        MvcResult cartResult = mockMvc.perform(post("/api/v1/carts/users/" + user.getId() + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalItemCount").value(2))
                .andExpect(jsonPath("$.data.totalAmount").value(3998.00))
                .andReturn();
        JsonNode cartRoot = objectMapper.readTree(cartResult.getResponse().getContentAsString());
        CartResponse cart = objectMapper.treeToValue(cartRoot.get("data"), CartResponse.class);
        assertThat(cart.getItems()).hasSize(1);
        UUID itemId = cart.getItems().get(0).getId();

        // 5. Update Cart item quantity to 1
        UpdateCartItemRequest updateItemReq = new UpdateCartItemRequest(1);
        mockMvc.perform(put("/api/v1/carts/users/" + user.getId() + "/items/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateItemReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalItemCount").value(1))
                .andExpect(jsonPath("$.data.totalAmount").value(1999.00));

        // 6. Checkout with Cash on Delivery
        CheckoutRequest checkoutReq = new CheckoutRequest(
                user.getId(),
                "456 Market St, San Francisco, CA",
                "Leave at front desk",
                PaymentMethod.CASH_ON_DELIVERY
        );

        MvcResult checkoutResult = mockMvc.perform(post("/api/v1/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.order.status").value(OrderStatus.PLACED.name()))
                .andExpect(jsonPath("$.data.payment.paymentMethod").value(PaymentMethod.CASH_ON_DELIVERY.name()))
                .andExpect(jsonPath("$.data.payment.status").value(PaymentStatus.PENDING.name()))
                .andExpect(jsonPath("$.data.payment.amount").value(1999.00))
                .andReturn();

        JsonNode checkoutRoot = objectMapper.readTree(checkoutResult.getResponse().getContentAsString());
        CheckoutResponse checkout = objectMapper.treeToValue(checkoutRoot.get("data"), CheckoutResponse.class);
        UUID orderId = checkout.getOrder().getId();
        UUID paymentId = checkout.getPayment().getId();

        // 7. Verify stock was deducted: was 10, bought 1 -> now 9
        mockMvc.perform(get("/api/v1/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.stockQuantity").value(9));

        // 8. Verify order can be retrieved
        mockMvc.perform(get("/api/v1/orders/" + orderId + "/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(orderId.toString()))
                .andExpect(jsonPath("$.data.items[0].productName").value("MacBook Pro 14"))
                .andExpect(jsonPath("$.data.payment.status").value("PENDING"));

        // 9. Collect Cash Payment on delivery
        PaymentCollectionRequest collectReq = new PaymentCollectionRequest("CASH-COLLECT-001", "Cash received upon doorstep delivery.");
        mockMvc.perform(post("/api/v1/payments/" + paymentId + "/users/" + user.getId() + "/collect-cash")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(collectReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value(PaymentStatus.COMPLETED.name()))
                .andExpect(jsonPath("$.data.transactionReference").value("CASH-COLLECT-001"))
                .andExpect(jsonPath("$.data.collectedAt").isNotEmpty());

        // 10. Verify order status transitioned to DELIVERED
        mockMvc.perform(get("/api/v1/orders/" + orderId + "/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value(OrderStatus.DELIVERED.name()))
                .andExpect(jsonPath("$.data.payment.status").value(PaymentStatus.COMPLETED.name()));
    }

    @Test
    @Order(4)
    void shouldRejectCheckoutWhenCartIsEmpty() throws Exception {
        UserRegisterRequest userReq = new UserRegisterRequest();
        userReq.setEmail("bob@example.com");
        userReq.setPassword("bobPassword123");
        userReq.setFirstName("Bob");
        userReq.setLastName("Jones");
        MvcResult userResult = mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReq)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode userRoot = objectMapper.readTree(userResult.getResponse().getContentAsString());
        UserResponse user = objectMapper.treeToValue(userRoot.get("data"), UserResponse.class);

        CheckoutRequest checkoutReq = new CheckoutRequest(
                user.getId(),
                "Bob Street",
                "Empty cart test",
                PaymentMethod.CASH_ON_DELIVERY
        );

        mockMvc.perform(post("/api/v1/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cannot checkout an empty cart. Please add items before checking out."))
                .andExpect(jsonPath("$.errors[0].error").value("Bad Request"))
                .andExpect(jsonPath("$.errors[0].message").value("Cannot checkout an empty cart. Please add items before checking out."));
    }

    @Test
    @Order(5)
    void shouldRejectAddingMoreQuantityThanAvailableStock() throws Exception {
        // Register user
        UserRegisterRequest userReq = new UserRegisterRequest();
        userReq.setEmail("stock.test@example.com");
        userReq.setPassword("stockPassword");
        userReq.setFirstName("Stock");
        userReq.setLastName("Tester");
        MvcResult userResult = mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReq)))
                .andExpect(status().isCreated())
                .andReturn();
        UserResponse user = objectMapper.readValue(objectMapper.readTree(userResult.getResponse().getContentAsString()).get("data").toString(), UserResponse.class);

        // Create Category and Brand
        CategoryRequest catReq = new CategoryRequest();
        catReq.setName("Limited Stock Category");
        MvcResult catResult = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(catReq)))
                .andExpect(status().isCreated())
                .andReturn();
        CategoryResponse category = objectMapper.readValue(objectMapper.readTree(catResult.getResponse().getContentAsString()).get("data").toString(), CategoryResponse.class);

        BrandRequest brandReq = new BrandRequest();
        brandReq.setName("Limited Brand");
        MvcResult brandResult = mockMvc.perform(post("/api/v1/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandReq)))
                .andExpect(status().isCreated())
                .andReturn();
        BrandResponse brand = objectMapper.readValue(objectMapper.readTree(brandResult.getResponse().getContentAsString()).get("data").toString(), BrandResponse.class);

        // Product with only 2 items in stock
        ProductRequest prodReq = new ProductRequest();
        prodReq.setName("Rare Collector Item");
        prodReq.setPrice(new BigDecimal("500.00"));
        prodReq.setStockQuantity(2);
        prodReq.setCategoryId(category.getId());
        prodReq.setBrandId(brand.getId());

        MvcResult prodResult = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prodReq)))
                .andExpect(status().isCreated())
                .andReturn();
        ProductResponse product = objectMapper.readValue(objectMapper.readTree(prodResult.getResponse().getContentAsString()).get("data").toString(), ProductResponse.class);

        // Attempt to add 5 items (exceeds stock of 2)
        AddToCartRequest cartReq = new AddToCartRequest(product.getId(), 5);
        mockMvc.perform(post("/api/v1/carts/users/" + user.getId() + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].error").value("Insufficient Stock"))
                .andExpect(jsonPath("$.errors[0].message").value("Requested quantity 5 exceeds available stock (2) for product: Rare Collector Item"));
    }

    @Test
    @Order(6)
    void shouldUpdateOrderStatusManually() throws Exception {
        // Register user
        UserRegisterRequest userReq = new UserRegisterRequest();
        userReq.setEmail("order.status.test@example.com");
        userReq.setPassword("password123");
        userReq.setFirstName("Status");
        userReq.setLastName("Tester");
        MvcResult userResult = mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReq)))
                .andExpect(status().isCreated())
                .andReturn();
        UserResponse user = objectMapper.readValue(objectMapper.readTree(userResult.getResponse().getContentAsString()).get("data").toString(), UserResponse.class);

        // Create Category and Brand
        CategoryRequest catReq = new CategoryRequest();
        catReq.setName("Status Cat");
        MvcResult catResult = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(catReq)))
                .andExpect(status().isCreated())
                .andReturn();
        CategoryResponse category = objectMapper.readValue(objectMapper.readTree(catResult.getResponse().getContentAsString()).get("data").toString(), CategoryResponse.class);

        BrandRequest brandReq = new BrandRequest();
        brandReq.setName("Status Brand");
        MvcResult brandResult = mockMvc.perform(post("/api/v1/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandReq)))
                .andExpect(status().isCreated())
                .andReturn();
        BrandResponse brand = objectMapper.readValue(objectMapper.readTree(brandResult.getResponse().getContentAsString()).get("data").toString(), BrandResponse.class);

        ProductRequest prodReq = new ProductRequest();
        prodReq.setName("Shipping Test Item");
        prodReq.setPrice(new BigDecimal("50.00"));
        prodReq.setStockQuantity(10);
        prodReq.setCategoryId(category.getId());
        prodReq.setBrandId(brand.getId());
        MvcResult prodResult = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prodReq)))
                .andExpect(status().isCreated())
                .andReturn();
        ProductResponse product = objectMapper.readValue(objectMapper.readTree(prodResult.getResponse().getContentAsString()).get("data").toString(), ProductResponse.class);

        // Add to cart and checkout
        mockMvc.perform(post("/api/v1/carts/users/" + user.getId() + "/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AddToCartRequest(product.getId(), 1))))
                .andExpect(status().isOk());

        MvcResult checkoutRes = mockMvc.perform(post("/api/v1/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CheckoutRequest(user.getId(), "Address", "Notes", PaymentMethod.CASH_ON_DELIVERY))))
                .andExpect(status().isOk())
                .andReturn();

        CheckoutResponse checkout = objectMapper.readValue(objectMapper.readTree(checkoutRes.getResponse().getContentAsString()).get("data").toString(), CheckoutResponse.class);
        UUID orderId = checkout.getOrder().getId();

        // Update order status to SHIPPED
        UpdateOrderStatusRequest updateReq = new UpdateOrderStatusRequest(OrderStatus.SHIPPED);
        mockMvc.perform(put("/api/v1/orders/" + orderId + "/users/" + user.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SHIPPED"));
    }

    @Test
    @Order(7)
    void shouldRejectNonCashPaymentMethodAtCheckout() throws Exception {
        // Register user
        UserRegisterRequest userReq = new UserRegisterRequest();
        userReq.setEmail("noncash.user@example.com");
        userReq.setPassword("password123");
        userReq.setFirstName("NoCash");
        userReq.setLastName("User");
        MvcResult userResult = mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReq)))
                .andExpect(status().isCreated())
                .andReturn();
        UserResponse user = objectMapper.readValue(objectMapper.readTree(userResult.getResponse().getContentAsString()).get("data").toString(), UserResponse.class);

        // Create Category and Brand
        CategoryRequest catReq = new CategoryRequest();
        catReq.setName("Cash Only Category");
        MvcResult catResult = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(catReq)))
                .andExpect(status().isCreated())
                .andReturn();
        CategoryResponse category = objectMapper.readValue(objectMapper.readTree(catResult.getResponse().getContentAsString()).get("data").toString(), CategoryResponse.class);

        BrandRequest brandReq = new BrandRequest();
        brandReq.setName("Cash Only Brand");
        MvcResult brandResult = mockMvc.perform(post("/api/v1/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandReq)))
                .andExpect(status().isCreated())
                .andReturn();
        BrandResponse brand = objectMapper.readValue(objectMapper.readTree(brandResult.getResponse().getContentAsString()).get("data").toString(), BrandResponse.class);

        // Product
        ProductRequest prodReq = new ProductRequest();
        prodReq.setName("Cash Only Product");
        prodReq.setPrice(new BigDecimal("99.99"));
        prodReq.setStockQuantity(5);
        prodReq.setCategoryId(category.getId());
        prodReq.setBrandId(brand.getId());
        MvcResult prodResult = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prodReq)))
                .andExpect(status().isCreated())
                .andReturn();
        ProductResponse product = objectMapper.readValue(objectMapper.readTree(prodResult.getResponse().getContentAsString()).get("data").toString(), ProductResponse.class);

        // Add to cart
        mockMvc.perform(post("/api/v1/carts/users/" + user.getId() + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddToCartRequest(product.getId(), 1))))
                .andExpect(status().isOk());

        // Attempt checkout with CREDIT_CARD (must fail with 400 Bad Request)
        CheckoutRequest checkoutReq = new CheckoutRequest(
                user.getId(),
                "123 Card Lane",
                "Try paying with credit card",
                PaymentMethod.CREDIT_CARD
        );

        mockMvc.perform(post("/api/v1/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].error").value("Payment Validation Error"))
                .andExpect(jsonPath("$.errors[0].field").value("paymentMethod"))
                .andExpect(jsonPath("$.message").value("Only cash payments (CASH_ON_DELIVERY or CASH) are supported at this time. Provided: CREDIT_CARD"));
    }

    @Test
    @Order(8)
    void shouldReturnValidationErrorsWhenPayloadIsInvalid() throws Exception {
        UserRegisterRequest invalidRequest = new UserRegisterRequest();
        invalidRequest.setEmail("not-a-valid-email");
        invalidRequest.setPassword(""); // empty password fails @NotBlank and @Size

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.field == 'email')].rejectedValue").value("not-a-valid-email"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
