package com.example.backend.repository;

import com.example.backend.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long>, JpaSpecificationExecutor<Coupon> {

    List<Coupon> findByStatus(Integer status);

    @Query("SELECT c FROM Coupon c WHERE c.status = 1 AND c.remainCount > 0 AND (c.startTime IS NULL OR c.startTime <= :now) AND (c.endTime IS NULL OR c.endTime >= :now)")
    List<Coupon> findAvailableCoupons(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM Coupon c WHERE c.status = 1 AND c.remainCount > 0 AND c.startTime <= :now AND c.endTime >= :now")
    List<Coupon> findValidCoupons(@Param("now") LocalDateTime now);
}