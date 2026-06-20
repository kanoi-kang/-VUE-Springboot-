package com.example.backend.repository;

import com.example.backend.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNo(String orderNo);

    Page<Order> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    Page<Order> findByUserIdAndStatus(Long userId, Integer status, Pageable pageable);

    Page<Order> findAllByOrderByCreateTimeDesc(Pageable pageable);

    Page<Order> findByStatus(Integer status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.orderNo LIKE %:keyword% OR o.userId IN (SELECT u.id FROM com.example.backend.entity.User u WHERE u.username LIKE %:keyword%)")
    Page<Order> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE (:status IS NULL OR o.status = :status) AND (o.orderNo LIKE %:keyword% OR o.userId IN (SELECT u.id FROM com.example.backend.entity.User u WHERE u.username LIKE %:keyword%))")
    Page<Order> searchByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") Integer status, Pageable pageable);

    boolean existsByOrderNo(String orderNo);

    List<Order> findByStatusAndCreateTimeBefore(Integer status, LocalDateTime time);

    Long countByStatus(Integer status);

    @Query("SELECT SUM(o.totalAmount) FROM Order o")
    Double sumTotalAmount();

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.createTime BETWEEN :start AND :end")
    Double sumTotalAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    Long countByCreateTimeBetween(LocalDateTime start, LocalDateTime end);
}