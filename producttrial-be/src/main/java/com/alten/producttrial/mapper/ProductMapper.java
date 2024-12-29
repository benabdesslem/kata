package com.alten.producttrial.mapper;

import com.alten.producttrial.dto.ProductDto;
import com.alten.producttrial.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductDto toDto(Product product);

    Product toEntity(ProductDto productDto);
}