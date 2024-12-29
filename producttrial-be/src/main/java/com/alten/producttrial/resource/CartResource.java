package com.alten.producttrial.resource;

import com.alten.producttrial.dto.CartItemDto;
import com.alten.producttrial.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@AllArgsConstructor
public class CartResource {
    private final CartService cartService;

    @Operation(
            summary = "Retrieve all items in the user's cart",
            description = "Fetches all the cart items for the currently authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart items retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping
    public ResponseEntity<List<CartItemDto>> getCartItems() {
        return ResponseEntity.ok(cartService.getCartItems());
    }

    @Operation(
            summary = "Add a product to the user's cart",
            description = "Adds the specified product to the cart of the currently authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product added to cart successfully"),
            @ApiResponse(responseCode = "404", description = "Product or user not found")
    })
    @PostMapping("/add")
    public ResponseEntity<CartItemDto> addToCart(@RequestParam Long productId) {
        CartItemDto cartItemDTO = cartService.addToCart(productId);
        return ResponseEntity.ok(cartItemDTO);
    }

    @Operation(
            summary = "Remove a product from the user's cart",
            description = "Removes the specified cart item from the currently authenticated user's cart."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cart item removed successfully"),
            @ApiResponse(responseCode = "404", description = "Cart item not found"),
            @ApiResponse(responseCode = "403", description = "Access denied to remove this cart item")
    })
    @DeleteMapping("/remove/{id}")
    public ResponseEntity<Void> removeFromCart(@PathVariable String id) {
        cartService.removeFromCart(id);
        return ResponseEntity.noContent().build();
    }
}
