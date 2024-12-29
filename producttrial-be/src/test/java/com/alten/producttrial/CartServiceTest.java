package com.alten.producttrial;

import com.alten.producttrial.dto.CartItemDto;
import com.alten.producttrial.dto.ProductDto;
import com.alten.producttrial.entity.CartItem;
import com.alten.producttrial.entity.Product;
import com.alten.producttrial.entity.User;
import com.alten.producttrial.exception.ResourceNotFoundException;
import com.alten.producttrial.exception.UserNotFoundException;
import com.alten.producttrial.mapper.CartItemMapper;
import com.alten.producttrial.repository.CartItemRepository;
import com.alten.producttrial.repository.ProductRepository;
import com.alten.producttrial.repository.UserRepository;
import com.alten.producttrial.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartItemMapper cartItemMapper;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;
    private CartItem cartItem;
    private CartItemDto cartItemDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setEmail("testuser@example.com");

        product = new Product();
        product.setId(1L);
        product.setName("Product 1");
        product.setPrice(100.0);

        cartItem = new CartItem();
        cartItem.setId("abc");
        cartItem.setProduct(product);
        cartItem.setUser(user);
        cartItem.setQuantity(1);

        ProductDto productDto = ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .build();

        cartItemDto = new CartItemDto();
        cartItemDto.setId("abc");
        cartItemDto.setProduct(productDto);
        cartItemDto.setQuantity(1);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(user.getEmail());
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void getCartItems_shouldReturnListOfCartItemDtos_whenUserExists() {
        // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(cartItemRepository.findAllByUserId(user.getId())).thenReturn(List.of(cartItem));
        when(cartItemMapper.toDto(cartItem)).thenReturn(cartItemDto);

        // Act
        var result = cartService.getCartItems();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(cartItemDto.getProduct().getId(), result.getFirst().getProduct().getId());

        // Verify interactions
        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(cartItemRepository, times(1)).findAllByUserId(user.getId());
    }

    @Test
    void addToCart_shouldAddProductToCart_whenProductExists() {
        // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartItemRepository.findByProductIdAndUserId(product.getId(), user.getId())).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartItemMapper.toDto(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem cartItem = invocation.getArgument(0);
            return Mappers.getMapper(CartItemMapper.class).toDto(cartItem);
        });

        // Act
        CartItemDto result = cartService.addToCart(product.getId());

        // Assert
        assertNotNull(result);
        assertEquals(cartItemDto.getProduct().getId(), result.getProduct().getId());
        assertEquals(1, result.getQuantity());

        // Verify interactions
        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(productRepository, times(1)).findById(product.getId());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void addToCart_shouldIncreaseQuantity_whenProductAlreadyInCart() {
        // // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartItemRepository.findByProductIdAndUserId(product.getId(), user.getId())).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartItemMapper.toDto(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem cartItem = invocation.getArgument(0);
            return Mappers.getMapper(CartItemMapper.class).toDto(cartItem);
        });
        // Act
        CartItemDto result = cartService.addToCart(product.getId());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getQuantity());

        // Verify interactions
        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(productRepository, times(1)).findById(product.getId());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void removeFromCart_shouldRemoveCartItem_whenUserIsAuthorized() {
        // // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(cartItemRepository.findById(cartItem.getId())).thenReturn(Optional.of(cartItem));

        // Act
        cartService.removeFromCart(cartItem.getId());

        // Verify interactions
        verify(cartItemRepository, times(1)).deleteById(cartItem.getId());
    }

    @Test
    void removeFromCart_shouldThrowAccessDeniedException_whenUserIsUnauthorized() {
        // // Arrange
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("anotheruser@example.com");
        when(userRepository.findByEmail("anotheruser@example.com")).thenReturn(Optional.of(new User()));
        when(cartItemRepository.findById(cartItem.getId())).thenReturn(Optional.of(cartItem));

        // Act and assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> cartService.removeFromCart(cartItem.getId()));

        assertEquals("Unauthorized to remove this item", exception.getMessage());
    }

    @Test
    void getCartItems_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        // // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        // Act and assert exception
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> cartService.getCartItems());

        assertEquals("User not found", exception.getReason());
    }

    @Test
    void addToCart_shouldThrowResourceNotFoundException_whenProductDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(productRepository.findById(product.getId())).thenReturn(Optional.empty());

        // Act and assert exception
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> cartService.addToCart(product.getId()));

        assertEquals("Product not found", exception.getReason());
    }
}
