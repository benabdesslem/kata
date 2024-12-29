package com.alten.producttrial.repository;

import com.alten.producttrial.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {

    Optional<CartItem> findByProductIdAndUserId(Long productId, Long userId);

    List<CartItem> findAllByUserId(Long id);
}