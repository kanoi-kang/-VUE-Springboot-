package com.example.backend.controller.api.admin;

import com.example.backend.common.BusinessException;
import com.example.backend.common.Result;
import com.example.backend.entity.ProductSpecification;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.ProductSpecificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/specifications")
public class AdminSpecificationController {

    @Autowired
    private ProductSpecificationRepository specificationRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/product/{productId}")
    public Result<List<ProductSpecification>> getSpecificationsByProductId(@PathVariable Long productId) {
        return Result.success(specificationRepository.findByProductId(productId));
    }

    @GetMapping("/{id}")
    public Result<ProductSpecification> getSpecificationById(@PathVariable Long id) {
        ProductSpecification spec = specificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Specification not found"));
        return Result.success(spec);
    }

    @PostMapping
    public Result<ProductSpecification> createSpecification(@RequestBody ProductSpecification specification) {
        if (!productRepository.existsById(specification.getProductId())) {
            throw new BusinessException(400, "Product not found");
        }
        return Result.success(specificationRepository.save(specification));
    }

    @PostMapping("/batch")
    public Result<List<ProductSpecification>> createSpecificationsBatch(@RequestBody List<ProductSpecification> specifications) {
        for (ProductSpecification spec : specifications) {
            if (!productRepository.existsById(spec.getProductId())) {
                throw new BusinessException(400, "Product not found: " + spec.getProductId());
            }
        }
        return Result.success(specificationRepository.saveAll(specifications));
    }

    @PutMapping("/{id}")
    public Result<ProductSpecification> updateSpecification(
            @PathVariable Long id,
            @RequestBody ProductSpecification specificationUpdate) {
        ProductSpecification spec = specificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Specification not found"));

        if (specificationUpdate.getName() != null) {
            spec.setName(specificationUpdate.getName());
        }
        if (specificationUpdate.getValue() != null) {
            spec.setValue(specificationUpdate.getValue());
        }
        if (specificationUpdate.getPrice() != null) {
            spec.setPrice(specificationUpdate.getPrice());
        }
        if (specificationUpdate.getStock() != null) {
            spec.setStock(specificationUpdate.getStock());
        }

        return Result.success(specificationRepository.save(spec));
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteSpecification(@PathVariable Long id) {
        if (!specificationRepository.existsById(id)) {
            throw new BusinessException(404, "Specification not found");
        }
        specificationRepository.deleteById(id);
        return Result.success("删除成功");
    }

    @DeleteMapping("/product/{productId}")
    @Transactional
    public Result<String> deleteSpecificationsByProductId(@PathVariable Long productId) {
        specificationRepository.deleteByProductId(productId);
        return Result.success("删除成功");
    }

    @DeleteMapping("/batch")
    @Transactional
    public Result<String> deleteSpecificationsBatch(@RequestBody List<Long> ids) {
        specificationRepository.deleteAllById(ids);
        return Result.success("批量删除成功");
    }

    @PutMapping("/{id}/stock")
    public Result<ProductSpecification> updateStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        ProductSpecification spec = specificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Specification not found"));

        int newStock = spec.getStock() + quantity;
        if (newStock < 0) {
            throw new BusinessException(400, "Stock cannot be negative");
        }

        spec.setStock(newStock);
        return Result.success(specificationRepository.save(spec));
    }

    @PutMapping("/{id}/price")
    public Result<ProductSpecification> updatePrice(
            @PathVariable Long id,
            @RequestParam BigDecimal price) {
        ProductSpecification spec = specificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Specification not found"));
        spec.setPrice(price);
        return Result.success(specificationRepository.save(spec));
    }
}
