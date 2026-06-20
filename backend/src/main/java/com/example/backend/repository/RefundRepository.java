package com.example.backend.repository;

import com.example.backend.entity.Refund;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long>, JpaSpecificationExecutor<Refund> {

    Page<Refund> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    Page<Refund> findByOrderIdOrderByCreateTimeDesc(Long orderId, Pageable pageable);

    Optional<Refund> findByRefundNo(String refundNo);

    Optional<Refund> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    Page<Refund> findAllByOrderByCreateTimeDesc(Pageable pageable);

    Page<Refund> findByStatusOrderByCreateTimeDesc(Integer status, Pageable pageable);

    Long countByStatus(Integer status);
}