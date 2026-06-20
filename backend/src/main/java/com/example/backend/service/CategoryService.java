package com.example.backend.service;

import com.example.backend.common.BusinessException;
import com.example.backend.dto.CategoryVO;
import com.example.backend.entity.Category;
import com.example.backend.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private static final String CATEGORY_TREE_CACHE_KEY = "categories:tree";

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public List<CategoryVO> getCategoryTree() {
        try {
            @SuppressWarnings("unchecked")
            List<CategoryVO> cachedTree = (List<CategoryVO>) redisTemplate.opsForValue().get(CATEGORY_TREE_CACHE_KEY);
            if (cachedTree != null) {
                return cachedTree;
            }
        } catch (Exception e) {
            // Redis unavailable, continue to fetch from database
        }

        List<Category> allCategories = categoryRepository.findAll();
        List<CategoryVO> tree = buildTree(allCategories, null);

        try {
            redisTemplate.opsForValue().set(CATEGORY_TREE_CACHE_KEY, tree, 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            // Redis unavailable, ignore cache update
        }

        return tree;
    }

    public List<Category> getCategoriesByParentId(Long parentId) {
        if (parentId == null) {
            return categoryRepository.findRootCategories();
        }
        return categoryRepository.findByParentIdOrderBySortAsc(parentId);
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Category not found"));
    }

    @Transactional
    public CategoryVO createCategory(Category category) {
        Category savedCategory = categoryRepository.save(category);
        clearCategoryCache();
        return convertToVO(savedCategory);
    }

    @Transactional
    public CategoryVO updateCategory(Long id, Category category) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Category not found"));

        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        existingCategory.setParentId(category.getParentId());
        existingCategory.setIcon(category.getIcon());
        existingCategory.setPic(category.getPic());
        existingCategory.setSort(category.getSort());
        existingCategory.setStatus(category.getStatus());

        Category updatedCategory = categoryRepository.save(existingCategory);
        clearCategoryCache();
        return convertToVO(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new BusinessException(404, "Category not found");
        }

        List<Category> children = categoryRepository.findByParentIdOrderBySortAsc(id);
        if (!children.isEmpty()) {
            throw new BusinessException(400, "Cannot delete category with children");
        }

        categoryRepository.deleteById(id);
        clearCategoryCache();
    }

    private List<CategoryVO> buildTree(List<Category> allCategories, Long parentId) {
        return allCategories.stream()
                .filter(c -> (parentId == null && c.getParentId() == null) ||
                             (parentId != null && parentId.equals(c.getParentId())))
                .map(c -> {
                    CategoryVO vo = convertToVO(c);
                    vo.setChildren(buildTree(allCategories, c.getId()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private void clearCategoryCache() {
        try {
            redisTemplate.delete(CATEGORY_TREE_CACHE_KEY);
        } catch (Exception e) {
            // Redis unavailable, ignore cache clear
        }
    }

    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setDescription(category.getDescription());
        vo.setParentId(category.getParentId());
        vo.setIcon(category.getIcon());
        vo.setPic(category.getPic());
        vo.setSort(category.getSort());
        vo.setStatus(category.getStatus());
        return vo;
    }
}