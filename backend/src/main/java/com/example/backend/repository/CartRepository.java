package com.example.backend.repository;

import com.example.backend.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByUserIdOrderByCreateTimeDesc(Long userId);

    Cart findByUserIdAndProductIdAndSpecificationId(Long userId, Long productId, Long specificationId);

    Cart findByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserId(Long userId);

    void deleteByUserIdAndId(Long userId, Long id);
}