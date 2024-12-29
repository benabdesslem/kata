package com.alten.producttrial.service;

import com.alten.producttrial.repository.ProductRepository;
import com.alten.producttrial.dto.ProductDto;
import com.alten.producttrial.entity.Product;
import com.alten.producttrial.exception.ResourceNotFoundException;
import com.alten.producttrial.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductDto createProduct(ProductDto dto) {
        Product product = productMapper.toEntity(dto);
        product.setCreatedAt(ZonedDateTime.now());
        product = productRepository.save(product);
        return productMapper.toDto(product);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setInventoryStatus(dto.getInventoryStatus());
        productRepository.save(product);
        return productMapper.toDto(product);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }
}
