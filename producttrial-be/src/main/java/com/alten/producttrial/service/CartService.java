package com.alten.producttrial.service;

import com.alten.producttrial.dto.CartItemDto;
import com.alten.producttrial.entity.CartItem;
import com.alten.producttrial.entity.Product;
import com.alten.producttrial.entity.User;
import com.alten.producttrial.exception.ResourceNotFoundException;
import com.alten.producttrial.exception.UserNotFoundException;
import com.alten.producttrial.mapper.CartItemMapper;
import com.alten.producttrial.repository.CartItemRepository;
import com.alten.producttrial.repository.ProductRepository;
import com.alten.producttrial.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartItemMapper cartItemMapper;

    @Transactional(readOnly = true)
    public List<CartItemDto> getCartItems() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<CartItem> cartItems = cartItemRepository.findAllByUserId(user.getId());
        return cartItems.stream()
                .map(cartItemMapper::toDto)
                .toList();
    }

    public CartItemDto addToCart(Long productId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        CartItem cartItem = cartItemRepository.findByProductIdAndUserId(productId, user.getId())
                .orElse(new CartItem());

        if (cartItem.getId() == null) {
            cartItem.setProduct(product);
            cartItem.setUser(user);
            cartItem.setQuantity(1);
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + 1);
        }

        CartItem savedItem = cartItemRepository.save(cartItem);
        return cartItemMapper.toDto(savedItem);
    }


    public void removeFromCart(String cartItemId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Unauthorized to remove this item");
        }

        cartItemRepository.deleteById(cartItemId);
    }

    public Optional<CartItemDto> decrementProductQuantity(Long productId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        CartItem cartItem = cartItemRepository.findByProductIdAndUserId(productId, user.getId()).
                orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (cartItem.getQuantity() > 1) {
            cartItem.setQuantity(cartItem.getQuantity() - 1);
            return Optional.of(cartItemMapper.toDto(cartItemRepository.save(cartItem)));
        }

        cartItemRepository.delete(cartItem);
        return Optional.empty();
    }
}
