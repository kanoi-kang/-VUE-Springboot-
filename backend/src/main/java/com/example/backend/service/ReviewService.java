package com.example.backend.service;

import com.example.backend.common.BusinessException;
import com.example.backend.entity.Product;
import com.example.backend.entity.Review;
import com.example.backend.entity.User;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.ReviewRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Review createReview(Long userId, Long productId, Long orderId, Integer rating, String content, String picUrls) {
        if (reviewRepository.existsByUserIdAndOrderIdAndProductId(userId, orderId, productId)) {
            throw new BusinessException("您已评价过该商品");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(productId);
        review.setOrderId(orderId);
        review.setRating(rating);
        review.setContent(content);
        review.setPicUrls(picUrls);
        return reviewRepository.save(review);
    }

    public Page<Map<String, Object>> getProductReviews(Long productId, Integer page, Integer size) {
        Page<Review> reviews = reviewRepository.findByProductIdAndIsShow(
                productId, 1, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime")));

        return reviews.map(review -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", review.getId());
            item.put("rating", review.getRating());
            item.put("content", review.getContent());
            item.put("picUrls", review.getPicUrls());
            item.put("createTime", review.getCreateTime());

            userRepository.findById(review.getUserId()).ifPresent(user -> {
                item.put("username", user.getNickname() != null ? user.getNickname() : user.getUsername());
                item.put("avatar", user.getAvatar());
            });
            return item;
        });
    }

    public Map<String, Object> getProductReviewStats(Long productId) {
        long total = reviewRepository.countByProductIdAndIsShow(productId, 1);
        Double avgRating = reviewRepository.getAverageRatingByProductId(productId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("averageRating", avgRating != null ? avgRating : 0);
        return stats;
    }

    public List<Map<String, Object>> getUserReviews(Long userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);
        return reviews.stream().map(review -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", review.getId());
            item.put("productId", review.getProductId());
            item.put("rating", review.getRating());
            item.put("content", review.getContent());
            item.put("createTime", review.getCreateTime());

            productRepository.findById(review.getProductId()).ifPresent(product -> {
                item.put("productName", product.getName());
                item.put("productPic", product.getPic());
            });
            return item;
        }).collect(Collectors.toList());
    }
}