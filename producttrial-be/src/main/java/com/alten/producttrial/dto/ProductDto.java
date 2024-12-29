package com.alten.producttrial.dto;

import com.alten.producttrial.enums.InventoryStatus;
import lombok.*;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {

    private Long id;

    private String code;

    private String name;

    private String description;

    private String image;

    private String category;

    private Double price;

    private Integer quantity;

    private String internalReference;

    private Long shellId;

    private InventoryStatus inventoryStatus;

    private Double rating;

    private ZonedDateTime createdAt;

    private ZonedDateTime updatedAt;
}