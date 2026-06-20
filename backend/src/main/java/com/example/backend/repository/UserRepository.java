package com.example.backend.repository;

import com.example.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Page<User> findByUsernameContaining(String keyword, Pageable pageable);

    Page<User> findByEmailContaining(String keyword, Pageable pageable);

    Page<User> findByNicknameContaining(String keyword, Pageable pageable);

    Page<User> findByStatus(Integer status, Pageable pageable);

    Page<User> findByUsernameContainingAndStatus(String keyword, Integer status, Pageable pageable);

    long countByStatus(Integer status);
}