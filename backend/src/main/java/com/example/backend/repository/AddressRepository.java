package com.example.backend.repository;

import com.example.backend.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserIdOrderByIsDefaultDescCreateTimeDesc(Long userId);

    Address findByUserIdAndIsDefault(Long userId, Integer isDefault);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = 0 WHERE a.userId = :userId AND a.id != :addressId")
    void clearDefaultByUserId(@Param("userId") Long userId, @Param("addressId") Long addressId);

    void deleteByUserId(Long userId);
}