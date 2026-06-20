package com.example.backend.repository;

import com.example.backend.entity.CouponUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponUserRepository extends JpaRepository<CouponUser, Long> {

    List<CouponUser> findByUserId(Long userId);

    List<CouponUser> findByUserIdAndStatus(Long userId, Integer status);

    Optional<CouponUser> findByUserIdAndCouponId(Long userId, Long couponId);

    long countByUserIdAndCouponId(Long userId, Long couponId);

    boolean existsByUserIdAndCouponIdAndStatus(Long userId, Long couponId, Integer status);
}