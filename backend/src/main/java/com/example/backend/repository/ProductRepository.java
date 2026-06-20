package com.example.backend.repository;

import com.example.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByIsPublishAndStatus(Integer isPublish, Integer status, Pageable pageable);

    Page<Product> findByCategoryIdAndIsPublishAndStatus(Long categoryId, Integer isPublish, Integer status, Pageable pageable);

    Page<Product> findByBrandIdAndIsPublishAndStatus(Long brandId, Integer isPublish, Integer status, Pageable pageable);

    Page<Product> findByNameContainingAndIsPublishAndStatus(String name, Integer isPublish, Integer status, Pageable pageable);

    Page<Product> findByIsPublish(Integer isPublish, Pageable pageable);

    Page<Product> findByBrandIdAndIsPublish(Long brandId, Integer isPublish, Pageable pageable);

    Page<Product> findByCategoryIdAndIsPublish(Long categoryId, Integer isPublish, Pageable pageable);

    Page<Product> findByNameContainingAndIsPublish(String name, Integer isPublish, Pageable pageable);
    
    // 新增管理端查询方法
    Page<Product> findByBrandIdAndStatus(Long brandId, Integer status, Pageable pageable);
    
    Page<Product> findByCategoryIdAndStatus(Long categoryId, Integer status, Pageable pageable);
    
    Page<Product> findByNameContainingAndStatus(String name, Integer status, Pageable pageable);
    
    Page<Product> findByStatus(Integer status, Pageable pageable);
    
    Page<Product> findByBrandId(Long brandId, Pageable pageable);
    
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    
    Page<Product> findByNameContaining(String name, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isNew = 1 AND p.isPublish = 1 AND p.status = 1 ORDER BY p.createTime DESC")
    List<Product> findNewProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isHot = 1 AND p.isPublish = 1 AND p.status = 1 ORDER BY p.sales DESC")
    List<Product> findHotProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isPublish = 1 AND p.status = 1 ORDER BY p.sales DESC")
    List<Product> findBestSellers(Pageable pageable);

    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :productId AND p.stock >= :quantity")
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock + :quantity WHERE p.id = :productId")
    int increaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE Product p SET p.sales = p.sales + :quantity WHERE p.id = :productId")
    int increaseSales(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE Product p SET p.viewCount = p.viewCount + 1 WHERE p.id = :productId")
    int incrementViewCount(@Param("productId") Long productId);

    @Query(value = "SELECT p.id, p.name, SUM(oi.quantity) as sales_count FROM products p " +
            "LEFT JOIN order_items oi ON p.id = oi.product_id " +
            "GROUP BY p.id, p.name ORDER BY sales_count DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopProducts(@Param("limit") Integer limit);
}