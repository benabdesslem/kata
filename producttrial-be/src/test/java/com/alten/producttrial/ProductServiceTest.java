package com.alten.producttrial;

import com.alten.producttrial.dto.ProductDto;
import com.alten.producttrial.entity.Product;
import com.alten.producttrial.enums.InventoryStatus;
import com.alten.producttrial.exception.ResourceNotFoundException;
import com.alten.producttrial.mapper.ProductMapper;
import com.alten.producttrial.repository.ProductRepository;
import com.alten.producttrial.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        product = new Product();
        product.setId(1L);
        product.setName("Product 1");
        product.setPrice(100.0);
        product.setInventoryStatus(InventoryStatus.INSTOCK);
        product.setCreatedAt(ZonedDateTime.now());

        productDto = new ProductDto();
        productDto.setId(1L);
        productDto.setName("Product 1");
        productDto.setPrice(100.0);
        productDto.setInventoryStatus(InventoryStatus.OUTOFSTOCK);
    }

    @Test
    void createProduct_shouldReturnProductDto() {
        when(productMapper.toEntity(any(ProductDto.class))).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDto(any(Product.class))).thenReturn(productDto);

        ProductDto result = productService.createProduct(productDto);

        assertNotNull(result);
        assertEquals(productDto.getName(), result.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void getAllProducts_shouldReturnListOfProductDtos() {
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(productMapper.toDto(any(Product.class))).thenReturn(productDto);

        var result = productService.getAllProducts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(productDto.getName(), result.getFirst().getName());
    }

    @Test
    void getProductById_shouldReturnProductDto_whenProductExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toDto(any(Product.class))).thenReturn(productDto);

        ProductDto result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(productDto.getName(), result.getName());
    }

    @Test
    void getProductById_shouldThrowResourceNotFoundException_whenProductDoesNotExist() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(1L));

        assertEquals("Product not found with id: 1", exception.getReason());
    }

    @Test
    void updateProduct_shouldReturnUpdatedProductDto() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toDto(any(Product.class))).thenReturn(productDto);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productDto.setName("Updated Product");

        ProductDto result = productService.updateProduct(1L, productDto);

        assertNotNull(result);
        assertEquals("Updated Product", result.getName());
    }

    @Test
    void updateProduct_shouldThrowResourceNotFoundException_whenProductDoesNotExist() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(1L, productDto));

        assertEquals("Product not found with id: 1", exception.getReason());
    }

    @Test
    void deleteProduct_shouldDeleteProduct_whenProductExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).delete(any(Product.class));
    }

    @Test
    void deleteProduct_shouldThrowResourceNotFoundException_whenProductDoesNotExist() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(1L));

        assertEquals("Product not found with id: 1", exception.getReason());
    }
}