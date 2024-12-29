package com.alten.producttrial;

import com.alten.producttrial.entity.CartItem;
import com.alten.producttrial.entity.Product;
import com.alten.producttrial.entity.User;
import com.alten.producttrial.enums.InventoryStatus;
import com.alten.producttrial.repository.CartItemRepository;
import com.alten.producttrial.repository.ProductRepository;
import com.alten.producttrial.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "testuser@example.com")
class CartResourceIT {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    private User user;

    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("test_user")
                .firstname("Test")
                .email("testuser@example.com")
                .password("password").build();

        userRepository.save(user);

        product = Product.builder()
                .code("P123")
                .name("Product1")
                .description("Description1")
                .image("image_url1")
                .category("Category1")
                .price(99.99)
                .quantity(100)
                .internalReference("InternalRef1")
                .shellId(1L)
                .inventoryStatus(InventoryStatus.INSTOCK)
                .rating(4.5)
                .createdAt(ZonedDateTime.now())
                .build();
        productRepository.save(product);
    }

    @AfterEach
    void tearDown() {
        cartItemRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldRetrieveAllCartItemsSuccessfully() throws Exception {
        // Arrange
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setUser(user);
        cartItem.setQuantity(1);
        cartItemRepository.save(cartItem);

        // Act & Assert
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].product.code", is(product.getCode())))
                .andExpect(jsonPath("$[0].quantity", is(1)));
    }

    @Test
    void shouldAddProductToCartSuccessfully() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/cart/add")
                        .param("productId", product.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product.code", is(product.getCode())))
                .andExpect(jsonPath("$.quantity", is(1)));
    }

    @Test
    void shouldReturn404WhenProductNotFoundForAdd() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/cart/add")
                        .param("productId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Product not found")));
    }

    @Test
    void shouldRemoveProductFromCartSuccessfully() throws Exception {
        // Arrange
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setUser(user);
        cartItem.setQuantity(1);
        cartItem = cartItemRepository.save(cartItem);

        // Act & Assert
        mockMvc.perform(delete("/api/cart/remove/{id}", cartItem.getId()))
                .andExpect(status().isNoContent());

        // Verify cart item is removed
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturn404WhenCartItemNotFoundForRemove() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/cart/remove/{id}", "nonexistent-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Cart item not found")));
    }

    @Test
    void shouldReturn401WhenAccessDeniedToRemoveCartItem() throws Exception {
        // Arrange
        User anotherUser = new User();
        anotherUser.setEmail("anotheruser@example.com");
        anotherUser.setPassword("password");
        userRepository.save(anotherUser);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setUser(anotherUser);
        cartItem.setQuantity(1);
        cartItem = cartItemRepository.save(cartItem);

        // Act & Assert
        mockMvc.perform(delete("/api/cart/remove/{id}", cartItem.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("Unauthorized to remove this item")));
    }


    @Test
    void shouldReduceProductQuantitySuccessfully() throws Exception {
        // Arrange: Add the product to the user's cart with quantity 1
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setUser(user);
        cartItem.setQuantity(1);
        cartItemRepository.save(cartItem);

        // Act & Assert: Send a PATCH request to reduce the quantity
        mockMvc.perform(patch("/api/cart/reduce-product-quantity")
                        .param("productId", product.getId().toString()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenProductNotFoundForReduce() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/cart/reduce-product-quantity")
                        .param("productId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Cart item not found")));
    }

    @Test
    void shouldReturn404WhenCartItemNotFoundForUser() throws Exception {
        // Arrange
        User anotherUser = new User();
        anotherUser.setEmail("anotheruser@example.com");
        anotherUser.setPassword("password");
        userRepository.save(anotherUser);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setUser(anotherUser);
        cartItem.setQuantity(1);
        cartItemRepository.save(cartItem);

        // Act & Assert
        mockMvc.perform(patch("/api/cart/reduce-product-quantity")
                        .param("productId", product.getId().toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Cart item not found")));

    }
}
