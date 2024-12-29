package com.alten.producttrial.mapper;

import com.alten.producttrial.dto.CartItemDto;
import com.alten.producttrial.entity.CartItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
    CartItemDto toDto(CartItem cartItem);
}
