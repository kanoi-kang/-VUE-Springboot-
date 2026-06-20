package com.example.backend.controller.api.admin;

import com.example.backend.common.PageResult;
import com.example.backend.common.Result;
import com.example.backend.entity.Brand;
import com.example.backend.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/brands")
public class AdminBrandController {

    @Autowired
    private BrandRepository brandRepository;

    @GetMapping
    public Result<PageResult<Brand>> getBrands(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "sort"));
        
        Specification<Brand> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(root.get("name"), "%" + keyword + "%"));
        }
        
        Page<Brand> brandPage = brandRepository.findAll(spec, pageable);
        return Result.success(PageResult.of(brandPage.getTotalElements(), page, size, brandPage.getContent()));
    }

    @GetMapping("/{id}")
    public Result<Brand> getBrandById(@PathVariable Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("品牌不存在"));
        return Result.success(brand);
    }

    @PostMapping
    public Result<Brand> createBrand(@RequestBody Brand brand) {
        return Result.success(brandRepository.save(brand));
    }

    @PutMapping("/{id}")
    public Result<Brand> updateBrand(@PathVariable Long id, @RequestBody Brand brand) {
        Brand existing = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("品牌不存在"));
        existing.setName(brand.getName());
        existing.setDescription(brand.getDescription());
        existing.setLogo(brand.getLogo());
        existing.setWebsite(brand.getWebsite());
        existing.setCountry(brand.getCountry());
        existing.setSort(brand.getSort());
        existing.setStatus(brand.getStatus());
        return Result.success(brandRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteBrand(@PathVariable Long id) {
        brandRepository.deleteById(id);
        return Result.success("删除成功");
    }
}