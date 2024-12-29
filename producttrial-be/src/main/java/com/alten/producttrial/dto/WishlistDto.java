package com.alten.producttrial.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDto {
    private Long id;
    private Long userId;
    private List<Long> productIds;
}
