package com.alten.producttrial.service;

import com.alten.producttrial.dto.WishlistDto;
import com.alten.producttrial.entity.Product;
import com.alten.producttrial.entity.User;
import com.alten.producttrial.entity.Wishlist;
import com.alten.producttrial.exception.ResourceNotFoundException;
import com.alten.producttrial.exception.UserNotFoundException;
import com.alten.producttrial.repository.ProductRepository;
import com.alten.producttrial.repository.UserRepository;
import com.alten.producttrial.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;


    @Transactional(readOnly = true)
    public WishlistDto getWishlistForCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Wishlist wishlist = wishlistRepository.findByUserId(user.getId())
                .orElseGet(() -> createWishlistForUser(user));

        return mapToDTO(wishlist);
    }


    @Transactional
    public WishlistDto addProductToWishlist(Long productId) {
        Wishlist wishlist = getOrCreateWishlistForCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        if (wishlist.getProducts() == null) {
            wishlist.setProducts(new ArrayList<>());
        }
        if (!wishlist.getProducts().contains(product)) {
            wishlist.getProducts().add(product);
        }

        return mapToDTO(wishlistRepository.save(wishlist));
    }


    @Transactional
    public WishlistDto removeProductFromWishlist(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));


        Wishlist wishlist = getOrCreateWishlistForCurrentUser();

        List<Product> products = new ArrayList<>(wishlist.getProducts());
        products.remove(product);
        wishlist.setProducts(products);

        return mapToDTO(wishlistRepository.save(wishlist));
    }


    private Wishlist getOrCreateWishlistForCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return wishlistRepository.findByUserId(user.getId())
                .orElseGet(() -> createWishlistForUser(user));
    }


    private Wishlist createWishlistForUser(User user) {
        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        return wishlistRepository.save(wishlist);
    }

    private WishlistDto mapToDTO(Wishlist wishlist) {
        List<Long> productIds = wishlist.getProducts().stream()
                .map(Product::getId)
                .collect(Collectors.toList());
        return new WishlistDto(wishlist.getId(), wishlist.getUser().getId(), productIds);
    }
}
