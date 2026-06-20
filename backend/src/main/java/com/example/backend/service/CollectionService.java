package com.example.backend.service;

import com.example.backend.entity.Collection;
import com.example.backend.entity.Product;
import com.example.backend.common.BusinessException;
import com.example.backend.repository.CollectionRepository;
import com.example.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CollectionService {

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Map<String, Object> addCollection(Long userId, Long productId) {
        if (collectionRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new BusinessException("已收藏过该商品");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        Collection collection = new Collection();
        collection.setUserId(userId);
        collection.setProductId(productId);
        collectionRepository.save(collection);

        product.setCollectCount(product.getCollectCount() + 1);
        productRepository.save(product);

        Map<String, Object> result = new HashMap<>();
        result.put("collected", true);
        result.put("collectCount", product.getCollectCount());
        return result;
    }

    @Transactional
    public Map<String, Object> removeCollection(Long userId, Long productId) {
        Collection collection = collectionRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new BusinessException("未收藏该商品"));

        collectionRepository.delete(collection);

        productRepository.findById(productId).ifPresent(product -> {
            product.setCollectCount(Math.max(0, product.getCollectCount() - 1));
            productRepository.save(product);
        });

        Map<String, Object> result = new HashMap<>();
        result.put("collected", false);
        return result;
    }

    public List<Map<String, Object>> getUserCollections(Long userId) {
        List<Collection> collections = collectionRepository.findByUserId(userId);
        return collections.stream().map(collection -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", collection.getId());
            item.put("productId", collection.getProductId());
            item.put("createTime", collection.getCreateTime());
            productRepository.findById(collection.getProductId()).ifPresent(product -> {
                item.put("productName", product.getName());
                item.put("productPic", product.getPic());
                item.put("productPrice", product.getPrice());
            });
            return item;
        }).collect(Collectors.toList());
    }

    public boolean isCollected(Long userId, Long productId) {
        return collectionRepository.existsByUserIdAndProductId(userId, productId);
    }
}