package com.example.backend.controller.api.admin;

import com.example.backend.common.Result;
import com.example.backend.common.PageResult;
import com.example.backend.dto.ProductRequest;
import com.example.backend.dto.ProductVO;
import com.example.backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public Result<PageResult<ProductVO>> getProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isPublish,
            @RequestParam(required = false) String keyword) {
        return Result.success(productService.getProductList(page, size, categoryId, brandId, status, isPublish, keyword));
    }

    @GetMapping("/{id}")
    public Result<ProductVO> getProductById(@PathVariable Long id) {
        return Result.success(productService.getProductById(id));
    }

    @PostMapping
    public Result<ProductVO> createProduct(@RequestBody ProductRequest request) {
        return Result.success(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    public Result<ProductVO> updateProduct(@PathVariable Long id, @RequestBody ProductRequest request) {
        return Result.success(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success(null);
    }
}