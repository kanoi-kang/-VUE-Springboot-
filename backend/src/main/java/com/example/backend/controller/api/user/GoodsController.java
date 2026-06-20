package com.example.backend.controller.api.user;

import com.example.backend.common.Result;
import com.example.backend.common.PageResult;
import com.example.backend.dto.CategoryVO;
import com.example.backend.entity.Brand;
import com.example.backend.entity.Category;
import com.example.backend.dto.ProductVO;
import com.example.backend.repository.BrandRepository;
import com.example.backend.service.CategoryService;
import com.example.backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandRepository brandRepository;

    @GetMapping("/categories/tree")
    public Result<List<CategoryVO>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }

    @GetMapping("/categories")
    public Result<List<Category>> getCategories(@RequestParam(required = false) Long parentId) {
        return Result.success(categoryService.getCategoriesByParentId(parentId));
    }

    @GetMapping("/products")
    public Result<PageResult<ProductVO>> getProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isPublish,
            @RequestParam(required = false) String keyword) {
        return Result.success(productService.getProductList(page, pageSize, categoryId, brandId, status, isPublish, keyword));
    }

    @GetMapping("/products/{id}")
    public Result<ProductVO> getProductById(@PathVariable Long id) {
        return Result.success(productService.getProductById(id));
    }

    @GetMapping("/products/hot")
    public Result<List<ProductVO>> getHotProducts(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(productService.getHotProducts(limit));
    }

    @GetMapping("/products/new")
    public Result<List<ProductVO>> getNewProducts(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(productService.getNewProducts(limit));
    }

    @GetMapping("/products/best-sellers")
    public Result<List<ProductVO>> getBestSellers(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(productService.getBestSellers(limit));
    }

    @GetMapping("/brands")
    public Result<List<Brand>> getBrands() {
        return Result.success(brandRepository.findByStatusOrderBySortAsc(1));
    }

    @GetMapping("/brands/{id}")
    public Result<Brand> getBrandById(@PathVariable Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("品牌不存在"));
        return Result.success(brand);
    }
}