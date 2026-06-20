package com.example.backend.controller.api.user;

import com.example.backend.common.Result;
import com.example.backend.dto.AddressRequest;
import com.example.backend.entity.Address;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    public Result<List<Address>> getAddressList(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<Address> addresses = addressService.getAddressList(userPrincipal.getUserId());
        return Result.success(addresses);
    }

    @GetMapping("/{id}")
    public Result<Address> getAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        Address address = addressService.getAddress(userPrincipal.getUserId(), id);
        return Result.success(address);
    }

    @PostMapping
    public Result<Address> createAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AddressRequest request) {
        Address address = addressService.createAddress(userPrincipal.getUserId(), request);
        return Result.success(address);
    }

    @PutMapping("/{id}")
    public Result<Address> updateAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {
        Address address = addressService.updateAddress(userPrincipal.getUserId(), id, request);
        return Result.success(address);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        addressService.deleteAddress(userPrincipal.getUserId(), id);
        return Result.success("Address deleted successfully");
    }

    @PutMapping("/{id}/default")
    public Result<?> setDefaultAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        addressService.setDefaultAddress(userPrincipal.getUserId(), id);
        return Result.success("Default address set successfully");
    }
}