package com.alten.producttrial.dto;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    private String id;
    private ProductDto product;
    private Integer quantity;
}
