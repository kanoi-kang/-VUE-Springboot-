package com.example.backend.repository;

import com.example.backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductIdAndIsShow(Long productId, Integer isShow, Pageable pageable);

    List<Review> findByUserId(Long userId);

    List<Review> findByOrderId(Long orderId);

    long countByProductIdAndIsShow(Long productId, Integer isShow);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId AND r.isShow = 1")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    boolean existsByUserIdAndOrderIdAndProductId(Long userId, Long orderId, Long productId);

    Page<Review> findByIsShow(Integer isShow, Pageable pageable);
}