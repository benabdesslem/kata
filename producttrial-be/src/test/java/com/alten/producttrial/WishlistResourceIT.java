package com.alten.producttrial;

import com.alten.producttrial.dto.AddProductRequest;
import com.alten.producttrial.dto.WishlistDto;
import com.alten.producttrial.entity.Product;
import com.alten.producttrial.entity.User;
import com.alten.producttrial.entity.Wishlist;
import com.alten.producttrial.enums.InventoryStatus;
import com.alten.producttrial.repository.ProductRepository;
import com.alten.producttrial.repository.UserRepository;
import com.alten.producttrial.repository.WishlistRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "testuser@example.com")
class WishlistResourceIT {

    @Autowired
    private WishlistRepository wishlistRepository;

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
        wishlistRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    void shouldRetrieveWishlistForCurrentUser() throws Exception {
        // Arrange
        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProducts(List.of(product));
        wishlistRepository.save(wishlist);

        // Act & Assert
        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productIds", hasSize(1)))
                .andExpect(jsonPath("$.productIds[0]").value(product.getId()));
    }

    @Test
    void shouldAddProductToWishlist() throws Exception {
        // Arrange
        AddProductRequest request = new AddProductRequest();
        request.setProductId(product.getId());

        WishlistDto wishlistDto = new WishlistDto();
        wishlistDto.setProductIds(Collections.singletonList(product.getId()));


        // Act & Assert
        mockMvc.perform(post("/api/wishlist/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.convertObjectToJsonBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productIds", hasSize(1)))
                .andExpect(jsonPath("$.productIds[0]").value(product.getId()));
    }

    @Test
    void shouldReturn404WhenProductNotFoundForAdd() throws Exception {
        // Arrange
        AddProductRequest request = new AddProductRequest();
        request.setProductId(999L);

        // Act & Assert
        mockMvc.perform(post("/api/wishlist/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.convertObjectToJsonBytes(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Product not found: 999")));
    }

    @Test
    @Transactional
    void shouldRemoveProductFromWishlist() throws Exception {
        // Arrange
        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProducts(List.of(product));
        wishlistRepository.save(wishlist);

        // Act & Assert
        mockMvc.perform(delete("/api/wishlist/remove")
                        .param("productId", product.getId().toString()))
                .andExpect(status().isNoContent());
        Wishlist updatedWishlist = wishlistRepository.findById(wishlist.getId())
                .orElseThrow(() -> new IllegalArgumentException("Wishlist not found"));

        assertThat(updatedWishlist.getProducts()).isEmpty();
    }

    @Test
    void shouldReturn404WhenProductNotFoundForRemove() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/wishlist/remove")
                        .param("productId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Product not found: 999")));
    }
}