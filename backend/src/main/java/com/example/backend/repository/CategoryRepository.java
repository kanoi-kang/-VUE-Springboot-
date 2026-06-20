package com.example.backend.repository;

import com.example.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByParentIdOrderBySortAsc(Long parentId);

    List<Category> findByStatusOrderBySortAsc(Integer status);

    @Query("SELECT c FROM Category c WHERE c.parentId IS NULL ORDER BY c.sort ASC")
    List<Category> findRootCategories();

    List<Category> findByNameContaining(String name);

    @Query(value = "SELECT c.id, c.name, COUNT(p.id) as product_count FROM categories c " +
            "LEFT JOIN products p ON c.id = p.category_id " +
            "GROUP BY c.id, c.name ORDER BY product_count DESC", nativeQuery = true)
    java.util.List<Object[]> countProductsByCategory();
}