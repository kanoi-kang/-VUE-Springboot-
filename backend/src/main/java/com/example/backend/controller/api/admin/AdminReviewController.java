package com.example.backend.controller.api.admin;

import com.example.backend.common.BusinessException;
import com.example.backend.common.PageResult;
import com.example.backend.common.Result;
import com.example.backend.entity.Review;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.ReviewRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/reviews")
public class AdminReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public Result<PageResult<ReviewVO>> getReviewList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer isShow,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Review> reviewPage;

        if (productId != null) {
            reviewPage = reviewRepository.findByProductIdAndIsShow(productId, isShow != null ? isShow : 1, pageable);
        } else if (isShow != null) {
            reviewPage = reviewRepository.findByIsShow(isShow, pageable);
        } else {
            reviewPage = reviewRepository.findAll(pageable);
        }

        List<ReviewVO> reviewVOs = reviewPage.getContent().stream()
                .map(review -> {
                    ReviewVO vo = new ReviewVO();
                    vo.setId(review.getId());
                    vo.setUserId(review.getUserId());
                    vo.setProductId(review.getProductId());
                    vo.setOrderId(review.getOrderId());
                    vo.setRating(review.getRating());
                    vo.setContent(review.getContent());
                    vo.setPicUrls(review.getPicUrls());
                    vo.setIsShow(review.getIsShow());
                    vo.setCreateTime(review.getCreateTime());

                    userRepository.findById(review.getUserId()).ifPresent(user -> {
                        vo.setUsername(user.getUsername());
                        vo.setNickname(user.getNickname());
                        vo.setAvatar(user.getAvatar());
                    });

                    productRepository.findById(review.getProductId()).ifPresent(product -> {
                        vo.setProductName(product.getName());
                    });

                    return vo;
                })
                .collect(Collectors.toList());

        return Result.success(PageResult.of(reviewPage.getTotalElements(), page, pageSize, reviewVOs));
    }

    @GetMapping("/{id}")
    public Result<ReviewVO> getReviewById(@PathVariable Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Review not found"));

        ReviewVO vo = new ReviewVO();
        vo.setId(review.getId());
        vo.setUserId(review.getUserId());
        vo.setProductId(review.getProductId());
        vo.setOrderId(review.getOrderId());
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        vo.setPicUrls(review.getPicUrls());
        vo.setIsShow(review.getIsShow());
        vo.setCreateTime(review.getCreateTime());
        vo.setUpdateTime(review.getUpdateTime());

        userRepository.findById(review.getUserId()).ifPresent(user -> {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        });

        return Result.success(vo);
    }

    @PutMapping("/{id}/show")
    public Result<Void> updateReviewShowStatus(@PathVariable Long id, @RequestParam Integer isShow) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Review not found"));
        review.setIsShow(isShow);
        reviewRepository.save(review);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    public Result<Review> updateReview(@PathVariable Long id, @RequestBody Review reviewUpdate) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Review not found"));

        if (reviewUpdate.getRating() != null) {
            review.setRating(reviewUpdate.getRating());
        }
        if (reviewUpdate.getContent() != null) {
            review.setContent(reviewUpdate.getContent());
        }
        if (reviewUpdate.getPicUrls() != null) {
            review.setPicUrls(reviewUpdate.getPicUrls());
        }
        if (reviewUpdate.getIsShow() != null) {
            review.setIsShow(reviewUpdate.getIsShow());
        }

        return Result.success(reviewRepository.save(review));
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteReview(@PathVariable Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new BusinessException(404, "Review not found");
        }
        reviewRepository.deleteById(id);
        return Result.success("删除成功");
    }

    @DeleteMapping("/batch")
    public Result<String> deleteReviewsBatch(@RequestBody List<Long> ids) {
        reviewRepository.deleteAllById(ids);
        return Result.success("批量删除成功");
    }

    public static class ReviewVO {
        private Long id;
        private Long userId;
        private Long productId;
        private String productName;
        private Long orderId;
        private Integer rating;
        private String content;
        private String picUrls;
        private Integer isShow;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private String username;
        private String nickname;
        private String avatar;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getPicUrls() { return picUrls; }
        public void setPicUrls(String picUrls) { this.picUrls = picUrls; }
        public Integer getIsShow() { return isShow; }
        public void setIsShow(Integer isShow) { this.isShow = isShow; }
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
        public LocalDateTime getUpdateTime() { return updateTime; }
        public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
    }
}