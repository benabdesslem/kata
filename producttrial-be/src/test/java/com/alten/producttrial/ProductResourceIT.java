package com.alten.producttrial;

import com.alten.producttrial.dto.ProductDto;
import com.alten.producttrial.entity.Product;
import com.alten.producttrial.enums.InventoryStatus;
import com.alten.producttrial.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Product Resource.
 * Ensures proper interaction between layers and validates API behavior.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
class ProductResourceIT {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void cleanDatabaseBeforeTest() {
        productRepository.deleteAll();
    }

    @AfterEach
    void cleanDatabaseAfterTest() {
        productRepository.deleteAll();
    }

    private ProductDto createValidProductDto() {
        return ProductDto.builder()
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
                .build();
    }

    private Product createSampleProduct(String code, String name, double price, int quantity, InventoryStatus status) {
        return Product.builder()
                .code(code)
                .name(name)
                .description("Sample Description")
                .image("sample_image_url")
                .category("Sample Category")
                .price(price)
                .quantity(quantity)
                .internalReference("InternalRef-" + code)
                .shellId(1L)
                .inventoryStatus(status)
                .rating(4.0)
                .createdAt(ZonedDateTime.now())
                .build();
    }

    @Test
    void shouldCreateProductSuccessfully() throws Exception {
        // Arrange
        ProductDto productDto = createValidProductDto();

        // Act & Assert
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.convertObjectToJsonBytes(productDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code", is(productDto.getCode())))
                .andExpect(jsonPath("$.name", is(productDto.getName())))
                .andExpect(jsonPath("$.inventoryStatus", is(productDto.getInventoryStatus().name())))
                .andExpect(jsonPath("$.price", is(productDto.getPrice())));
    }

    @Test
    void shouldRetrieveAllProducts() throws Exception {
        // Arrange
        Product product1 = createSampleProduct("P124", "Product2", 79.99, 150, InventoryStatus.INSTOCK);
        Product product2 = createSampleProduct("P125", "Product3", 199.99, 200, InventoryStatus.OUTOFSTOCK);
        productRepository.saveAll(List.of(product1, product2));

        // Act & Assert
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].code", is(product1.getCode())))
                .andExpect(jsonPath("$[0].name", is(product1.getName())))
                .andExpect(jsonPath("$[1].code", is(product2.getCode())))
                .andExpect(jsonPath("$[1].name", is(product2.getName())));
    }

    @Test
    void shouldReturnEmptyListWhenNoProductsExist() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldRetrieveProductById() throws Exception {
        // Arrange
        Product product = createSampleProduct("P126", "Product4", 129.99, 50, InventoryStatus.INSTOCK);
        product = productRepository.save(product);

        // Act & Assert
        mockMvc.perform(get("/api/products/{id}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(product.getId().intValue())))
                .andExpect(jsonPath("$.code", is(product.getCode())))
                .andExpect(jsonPath("$.name", is(product.getName())));
    }

    @Test
    void shouldReturn404WhenProductNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/{id}", 100))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.errorKey", is("resource_not_found")))
                .andExpect(jsonPath("$.message", is("Product not found with id: 100")));
    }

    @Test
    void shouldUpdateProductSuccessfully() throws Exception {
        // Arrange
        Product product = createSampleProduct("P127", "Product5", 59.99, 10, InventoryStatus.INSTOCK);
        product = productRepository.save(product);
        ProductDto updateDto = ProductDto.builder()
                .name("Updated Product5")
                .price(99.99)
                .inventoryStatus(InventoryStatus.OUTOFSTOCK)
                .build();

        // Act & Assert
        mockMvc.perform(patch("/api/products/{id}", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.convertObjectToJsonBytes(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(product.getId().intValue())))
                .andExpect(jsonPath("$.name", is(updateDto.getName())))
                .andExpect(jsonPath("$.price", is(updateDto.getPrice())))
                .andExpect(jsonPath("$.inventoryStatus", is(updateDto.getInventoryStatus().name())));
    }


    @Test
    void shouldReturn404WhenProductNotFoundForUpdate() throws Exception {
        // Arrange
        ProductDto updateDto = ProductDto.builder()
                .name("Updated Product5")
                .price(99.99)
                .inventoryStatus(InventoryStatus.OUTOFSTOCK)
                .build();

        // Act & Assert
        mockMvc.perform(patch("/api/products/{id}", 100)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.convertObjectToJsonBytes(updateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.errorKey", is("resource_not_found")))
                .andExpect(jsonPath("$.message", is("Product not found with id: 100")));
    }

    @Test
    void shouldDeleteProductSuccessfully() throws Exception {
        // Arrange
        Product product = createSampleProduct("P128", "Product6", 49.99, 20, InventoryStatus.INSTOCK);
        product = productRepository.save(product);

        // Act & Assert
        mockMvc.perform(delete("/api/products/{id}", product.getId()))
                .andExpect(status().isNoContent());

        // Verify
        mockMvc.perform(get("/api/products/{id}", product.getId()))
                .andExpect(status().isNotFound());
    }
}
