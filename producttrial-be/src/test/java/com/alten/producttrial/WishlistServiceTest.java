package com.alten.producttrial;

import com.alten.producttrial.dto.WishlistDto;
import com.alten.producttrial.entity.Product;
import com.alten.producttrial.entity.User;
import com.alten.producttrial.entity.Wishlist;
import com.alten.producttrial.exception.ResourceNotFoundException;
import com.alten.producttrial.exception.UserNotFoundException;
import com.alten.producttrial.repository.ProductRepository;
import com.alten.producttrial.repository.UserRepository;
import com.alten.producttrial.repository.WishlistRepository;
import com.alten.producttrial.service.WishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private WishlistService wishlistService;

    private User user;
    private Product product;
    private Wishlist wishlist;

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

        wishlist = new Wishlist();
        wishlist.setId(1L);
        wishlist.setUser(user);
        wishlist.setProducts(List.of(product));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(user.getEmail());
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void getWishlistForCurrentUser_shouldReturnWishlistDto_whenUserExists() {
        // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(wishlistRepository.findByUserId(user.getId())).thenReturn(Optional.of(wishlist));

        // Act
        WishlistDto result = wishlistService.getWishlistForCurrentUser();

        // Assert
        assertNotNull(result);
        assertEquals(user.getId(), result.getUserId());
        assertEquals(1, result.getProductIds().size());
        assertEquals(product.getId(), result.getProductIds().getFirst());

        // Verify interactions
        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(wishlistRepository, times(1)).findByUserId(user.getId());
    }

    @Test
    void addProductToWishlist_shouldAddProductToWishlist_whenProductExists() {
        // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProducts(new ArrayList<>());
        when(wishlistRepository.findByUserId(user.getId())).thenReturn(Optional.of(wishlist));

        WishlistDto wishlistDto = new WishlistDto();
        wishlistDto.setId(wishlist.getId());
        wishlistDto.setUserId(user.getId());
        wishlistDto.setProductIds(List.of(product.getId()));

        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(wishlist);

        // Act
        WishlistDto result = wishlistService.addProductToWishlist(product.getId());

        // Assert
        assertNotNull(result);
        assertTrue(result.getProductIds().contains(product.getId()));
        assertEquals(1, result.getProductIds().size());

        // Verify interactions
        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(productRepository, times(1)).findById(product.getId());
        verify(wishlistRepository, times(1)).save(any(Wishlist.class));
    }

    @Test
    void addProductToWishlist_shouldThrowResourceNotFoundException_whenProductDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(productRepository.findById(product.getId())).thenReturn(Optional.empty());

        // Act and Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> wishlistService.addProductToWishlist(product.getId()));
        assertEquals("Product not found: " + product.getId(), exception.getReason());
    }

    @Test
    void removeProductFromWishlist_shouldRemoveProductFromWishlist_whenProductExists() {
        // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        List<Product> products = new ArrayList<>();
        products.add(product);
        wishlist.setProducts(products);

        when(wishlistRepository.findByUserId(user.getId())).thenReturn(Optional.of(wishlist));

        WishlistDto wishlistDto = new WishlistDto();
        wishlistDto.setId(wishlist.getId());
        wishlistDto.setUserId(user.getId());
        wishlistDto.setProductIds(new ArrayList<>());

        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(wishlist); // Mock la sauvegarde de la wishlist

        // Act
        WishlistDto result = wishlistService.removeProductFromWishlist(product.getId());

        // Assert
        assertNotNull(result);
        assertTrue(result.getProductIds().isEmpty());

        // Verify interactions
        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(productRepository, times(1)).findById(product.getId());
        verify(wishlistRepository, times(1)).save(any(Wishlist.class));
    }

    @Test
    void removeProductFromWishlist_shouldThrowResourceNotFoundException_whenProductDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(productRepository.findById(product.getId())).thenReturn(Optional.empty());

        // Act and Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> wishlistService.removeProductFromWishlist(product.getId()));
        assertEquals("Product not found: " + product.getId(), exception.getReason());
    }

    @Test
    void getWishlistForCurrentUser_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        // Act and Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> wishlistService.getWishlistForCurrentUser());
        assertEquals("User not found", exception.getReason());
    }
}