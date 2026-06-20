package com.example.backend.service;

import com.example.backend.common.BusinessException;
import com.example.backend.dto.AddressRequest;
import com.example.backend.entity.Address;
import com.example.backend.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    public List<Address> getAddressList(Long userId) {
        return addressRepository.findByUserIdOrderByIsDefaultDescCreateTimeDesc(userId);
    }

    public Address getAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(404, "Address not found"));

        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(403, "Access denied");
        }

        return address;
    }

    @Transactional
    public Address createAddress(Long userId, AddressRequest request) {
        Address address = new Address();
        address.setUserId(userId);
        address.setConsignee(request.getConsignee());
        address.setPhone(request.getPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());
        address.setPostalCode(request.getPostalCode());
        address.setTag(request.getTag());

        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            address.setIsDefault(1);
            addressRepository.clearDefaultByUserId(userId, 0L);
        } else {
            address.setIsDefault(0);
        }

        return addressRepository.save(address);
    }

    @Transactional
    public Address updateAddress(Long userId, Long addressId, AddressRequest request) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(404, "Address not found"));

        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(403, "Access denied");
        }

        address.setConsignee(request.getConsignee());
        address.setPhone(request.getPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());
        address.setPostalCode(request.getPostalCode());
        address.setTag(request.getTag());

        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            address.setIsDefault(1);
            addressRepository.clearDefaultByUserId(userId, addressId);
        }

        return addressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(404, "Address not found"));

        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(403, "Access denied");
        }

        addressRepository.delete(address);
    }

    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(404, "Address not found"));

        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(403, "Access denied");
        }

        addressRepository.clearDefaultByUserId(userId, addressId);
        address.setIsDefault(1);
        addressRepository.save(address);
    }

    public List<Address> convertToAddressList(List<Address> addresses) {
        return addresses.stream().map(this::convertToAddress).collect(Collectors.toList());
    }

    private Address convertToAddress(Address address) {
        return address;
    }
}