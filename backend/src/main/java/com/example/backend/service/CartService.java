package com.example.backend.service;

import com.example.backend.common.BusinessException;
import com.example.backend.dto.CartAddRequest;
import com.example.backend.dto.CartVO;
import com.example.backend.entity.Cart;
import com.example.backend.entity.Product;
import com.example.backend.repository.CartRepository;
import com.example.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<CartVO> getCartList(Long userId) {
        List<Cart> carts = cartRepository.findByUserIdOrderByCreateTimeDesc(userId);
        
        return carts.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CartVO addToCart(Long userId, CartAddRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(404, "Product not found"));

        if (product.getIsPublish() == null || product.getIsPublish() != 1) {
            throw new BusinessException(400, "该商品已下架");
        }
        if (product.getStatus() == null || product.getStatus() != 1) {
            throw new BusinessException(400, "该商品已下架");
        }
        if (product.getStock() < request.getQuantity()) {
            throw new BusinessException(400, "Insufficient stock");
        }

        Cart existingCart = null;
        if (request.getSpecificationId() != null) {
            existingCart = cartRepository.findByUserIdAndProductIdAndSpecificationId(
                    userId, request.getProductId(), request.getSpecificationId());
        } else {
            existingCart = cartRepository.findByUserIdAndProductId(userId, request.getProductId());
        }

        Cart cart;
        if (existingCart != null) {
            int newQuantity = existingCart.getQuantity() + request.getQuantity();
            if (product.getStock() < newQuantity) {
                throw new BusinessException(400, "Insufficient stock");
            }
            existingCart.setQuantity(newQuantity);
            cart = cartRepository.save(existingCart);
        } else {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(request.getProductId());
            cart.setQuantity(request.getQuantity());
            cart.setPrice(product.getPrice());
            cart.setSpecificationId(request.getSpecificationId());
            cart.setSpecName(request.getSpecName());
            cart = cartRepository.save(cart);
        }

        return convertToVO(cart);
    }

    @Transactional
    public void updateCartItem(Long userId, Long cartId, Integer quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new BusinessException(404, "Cart item not found"));

        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException(403, "Access denied");
        }

        Product product = productRepository.findById(cart.getProductId())
                .orElseThrow(() -> new BusinessException(404, "Product not found"));

        Integer stock = product.getStock() != null ? product.getStock() : 0;
        if (stock < quantity) {
            throw new BusinessException(400, "Insufficient stock");
        }

        cart.setQuantity(quantity);
        cartRepository.save(cart);
    }

    @Transactional
    public void removeCartItem(Long userId, Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new BusinessException(404, "Cart item not found"));

        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException(403, "Access denied");
        }

        cartRepository.delete(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartRepository.deleteByUserId(userId);
    }

    private CartVO convertToVO(Cart cart) {
        CartVO vo = new CartVO();
        vo.setId(cart.getId());
        vo.setProductId(cart.getProductId());
        vo.setQuantity(cart.getQuantity());
        vo.setPrice(cart.getPrice());
        vo.setTotalPrice(cart.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
        vo.setSpecificationId(cart.getSpecificationId());
        vo.setSpecName(cart.getSpecName());
        vo.setCreateTime(cart.getCreateTime());

        productRepository.findById(cart.getProductId()).ifPresent(product -> {
            vo.setProductName(product.getName());
            vo.setPic(product.getPic());
        });

        return vo;
    }
}