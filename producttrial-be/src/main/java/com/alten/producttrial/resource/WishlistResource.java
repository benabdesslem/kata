package com.alten.producttrial.resource;

import com.alten.producttrial.dto.AddProductRequest;
import com.alten.producttrial.dto.WishlistDto;
import com.alten.producttrial.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistResource {

    private final WishlistService wishlistService;

    @Operation(
            summary = "Retrieve the wishlist for the currently authenticated user",
            description = "Fetches the wishlist of the user that is currently authenticated."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wishlist retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping
    public ResponseEntity<WishlistDto> getWishlistForCurrentUser() {
        return ResponseEntity.ok(wishlistService.getWishlistForCurrentUser());
    }

    @Operation(
            summary = "Add a product to the user's wishlist",
            description = "Adds the specified product to the wishlist of the currently authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product added to wishlist successfully"),
            @ApiResponse(responseCode = "404", description = "Product or user not found")
    })
    @PostMapping("/add")
    public ResponseEntity<WishlistDto> addProductToWishlist(@RequestBody AddProductRequest request) {
        return ResponseEntity.ok(wishlistService.addProductToWishlist(request.getProductId()));
    }

    @Operation(
            summary = "Remove a product from the user's wishlist",
            description = "Removes the specified product from the wishlist of the currently authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product removed from wishlist successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "403", description = "Access denied to remove this product")
    })
    @DeleteMapping("/remove")
    public ResponseEntity<WishlistDto> removeProductFromWishlist(@RequestParam Long productId) {
        wishlistService.removeProductFromWishlist(productId);
        return ResponseEntity.noContent().build();
    }
}
