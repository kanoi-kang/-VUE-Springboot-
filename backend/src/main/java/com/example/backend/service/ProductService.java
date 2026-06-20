package com.example.backend.service;

import com.example.backend.common.BusinessException;
import com.example.backend.common.PageResult;
import com.example.backend.dto.ProductRequest;
import com.example.backend.dto.ProductVO;
import com.example.backend.dto.ProductImageVO;
import com.example.backend.dto.ProductSpecificationVO;
import com.example.backend.entity.Product;
import com.example.backend.entity.ProductImage;
import com.example.backend.entity.ProductSpecification;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.ProductImageRepository;
import com.example.backend.repository.ProductSpecificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final String PRODUCT_CACHE_PREFIX = "product:";
    private static final int CACHE_EXPIRE_MINUTES = 30;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductSpecificationRepository productSpecificationRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public PageResult<ProductVO> getProductList(Integer page, Integer pageSize, Long categoryId, Long brandId, Integer status, Integer isPublish, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));

        Page<Product> productPage;
        if (isPublish != null && status != null) {
            if (brandId != null) {
                productPage = productRepository.findByBrandIdAndIsPublishAndStatus(brandId, isPublish, status, pageable);
            } else if (categoryId != null) {
                productPage = productRepository.findByCategoryIdAndIsPublishAndStatus(categoryId, isPublish, status, pageable);
            } else if (StringUtils.hasText(keyword)) {
                productPage = productRepository.findByNameContainingAndIsPublishAndStatus(keyword, isPublish, status, pageable);
            } else {
                productPage = productRepository.findByIsPublishAndStatus(isPublish, status, pageable);
            }
        } else if (status != null) {
            productPage = productRepository.findByStatus(status, pageable);
        } else if (isPublish != null) {
            if (brandId != null) {
                productPage = productRepository.findByBrandIdAndIsPublish(brandId, isPublish, pageable);
            } else if (categoryId != null) {
                productPage = productRepository.findByCategoryIdAndIsPublish(categoryId, isPublish, pageable);
            } else if (StringUtils.hasText(keyword)) {
                productPage = productRepository.findByNameContainingAndIsPublish(keyword, isPublish, pageable);
            } else {
                productPage = productRepository.findByIsPublish(isPublish, pageable);
            }
        } else if (brandId != null) {
            productPage = productRepository.findByBrandId(brandId, pageable);
        } else if (categoryId != null) {
            productPage = productRepository.findByCategoryId(categoryId, pageable);
        } else if (StringUtils.hasText(keyword)) {
            productPage = productRepository.findByNameContaining(keyword, pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }

        List<ProductVO> productVOs = productPage.getContent().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(productPage.getTotalElements(), page, pageSize, productVOs);
    }

    public ProductVO getProductById(Long id) {
        String cacheKey = PRODUCT_CACHE_PREFIX + id;

        try {
            @SuppressWarnings("unchecked")
            ProductVO cachedProduct = (ProductVO) redisTemplate.opsForValue().get(cacheKey);
            if (cachedProduct != null) {
                productRepository.incrementViewCount(id);
                return cachedProduct;
            }
        } catch (Exception e) {
            // Redis unavailable, continue to fetch from database
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Product not found"));

        ProductVO vo = convertToVO(product);

        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortAsc(id);
        vo.setImages(images.stream().map(this::convertToImageVO).collect(Collectors.toList()));
        
        // 从 product_images 表同步更新 pics 字段，确保前端能正确加载
        if (!images.isEmpty()) {
            List<String> imageUrls = images.stream()
                    .map(ProductImage::getUrl)
                    .collect(Collectors.toList());
            vo.setPics(imageUrls);
        } else if (product.getPics() != null && !product.getPics().isEmpty()) {
            // 如果 product_images 表没有数据，尝试从 products.pics 字段获取
            vo.setPics(Arrays.asList(product.getPics().split(",")));
        }

        List<ProductSpecification> specifications = productSpecificationRepository.findByProductId(id);
        vo.setSpecifications(specifications.stream().map(this::convertToSpecVO).collect(Collectors.toList()));

        try {
            redisTemplate.opsForValue().set(cacheKey, vo, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            productRepository.incrementViewCount(id);
        } catch (Exception e) {
            // Redis unavailable, ignore cache update
        }

        return vo;
    }

    public List<ProductVO> getHotProducts(Integer limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findHotProducts(pageable).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    public List<ProductVO> getNewProducts(Integer limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findNewProducts(pageable).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    public List<ProductVO> getBestSellers(Integer limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findBestSellers(pageable).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductVO createProduct(ProductRequest request) {
        Product product = convertToEntity(request);
        Product savedProduct = productRepository.save(product);

        if (request.getPics() != null && !request.getPics().isEmpty()) {
            saveProductImages(savedProduct.getId(), request.getPics());
        }

        clearProductCache(savedProduct.getId());
        return convertToVO(savedProduct);
    }

    @Transactional
    public ProductVO updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Product not found"));

        updateProductFromRequest(product, request);
        Product updatedProduct = productRepository.save(product);

        if (request.getPics() != null) {
            productImageRepository.deleteByProductId(id);
            saveProductImages(id, request.getPics());
        }

        clearProductCache(id);
        return convertToVO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new BusinessException(404, "Product not found");
        }
        productRepository.deleteById(id);
        productImageRepository.deleteByProductId(id);
        productSpecificationRepository.deleteByProductId(id);
        clearProductCache(id);
    }

    @Transactional
    public void updateStock(Long productId, Integer quantity) {
        int result = productRepository.decreaseStock(productId, quantity);
        if (result == 0) {
            throw new BusinessException(400, "Insufficient stock");
        }
    }

    private void saveProductImages(Long productId, List<String> pics) {
        for (int i = 0; i < pics.size(); i++) {
            ProductImage image = new ProductImage();
            image.setProductId(productId);
            image.setUrl(pics.get(i));
            image.setSort(i);
            productImageRepository.save(image);
        }
    }

    private void clearProductCache(Long productId) {
        try {
            redisTemplate.delete(PRODUCT_CACHE_PREFIX + productId);
        } catch (Exception e) {
            // Redis unavailable, ignore cache clear
        }
    }

    private ProductVO convertToVO(Product product) {
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setName(product.getName());
        vo.setProductSn(product.getProductSn());
        vo.setDescription(product.getDescription());
        vo.setContent(product.getContent());
        vo.setCategoryId(product.getCategoryId());
        vo.setBrandId(product.getBrandId());
        vo.setPrice(product.getPrice());
        vo.setOriginalPrice(product.getOriginalPrice());
        vo.setCostPrice(product.getCostPrice());
        vo.setStock(product.getStock());
        vo.setLowStock(product.getLowStock());
        vo.setPic(product.getPic());
        vo.setUnit(product.getUnit());
        vo.setStatus(product.getStatus());
        vo.setIsPublish(product.getIsPublish());
        vo.setIsNew(product.getIsNew());
        vo.setIsHot(product.getIsHot());
        vo.setSales(product.getSales());
        vo.setViewCount(product.getViewCount());
        vo.setCollectCount(product.getCollectCount());
        vo.setWeight(product.getWeight());
        vo.setCreateTime(product.getCreateTime());
        vo.setUpdateTime(product.getUpdateTime());

        if (product.getPics() != null && !product.getPics().isEmpty()) {
            vo.setPics(Arrays.asList(product.getPics().split(",")));
        }

        return vo;
    }

    private void updateProductFromRequest(Product product, ProductRequest request) {
        if (request.getName() != null) product.setName(request.getName());
        if (request.getProductSn() != null) product.setProductSn(request.getProductSn());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getContent() != null) product.setContent(request.getContent());
        if (request.getCategoryId() != null) product.setCategoryId(request.getCategoryId());
        if (request.getBrandId() != null) product.setBrandId(request.getBrandId());
        if (request.getPrice() != null) product.setPrice(new java.math.BigDecimal(request.getPrice()));
        if (request.getOriginalPrice() != null) product.setOriginalPrice(new java.math.BigDecimal(request.getOriginalPrice()));
        if (request.getCostPrice() != null) product.setCostPrice(new java.math.BigDecimal(request.getCostPrice()));
        if (request.getStock() != null) product.setStock(request.getStock());
        if (request.getLowStock() != null) product.setLowStock(request.getLowStock());
        if (request.getPic() != null) product.setPic(request.getPic());
        if (request.getPics() != null) product.setPics(String.join(",", request.getPics()));
        if (request.getUnit() != null) product.setUnit(request.getUnit());
        if (request.getStatus() != null) product.setStatus(request.getStatus());
        if (request.getIsPublish() != null) product.setIsPublish(request.getIsPublish());
        if (request.getIsNew() != null) product.setIsNew(request.getIsNew());
        if (request.getIsHot() != null) product.setIsHot(request.getIsHot());
        if (request.getWeight() != null) product.setWeight(new java.math.BigDecimal(request.getWeight()));
    }

    private Product convertToEntity(ProductRequest request) {
        Product product = new Product();
        updateProductFromRequest(product, request);
        return product;
    }

    private ProductImageVO convertToImageVO(ProductImage image) {
        ProductImageVO vo = new ProductImageVO();
        vo.setId(image.getId());
        vo.setUrl(image.getUrl());
        vo.setType(image.getType());
        vo.setSort(image.getSort());
        return vo;
    }

    private ProductSpecificationVO convertToSpecVO(ProductSpecification spec) {
        ProductSpecificationVO vo = new ProductSpecificationVO();
        vo.setId(spec.getId());
        vo.setName(spec.getName());
        vo.setValue(spec.getValue());
        vo.setPrice(spec.getPrice());
        vo.setStock(spec.getStock());
        return vo;
    }
}