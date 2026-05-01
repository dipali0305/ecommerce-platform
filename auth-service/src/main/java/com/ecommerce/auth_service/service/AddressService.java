package com.ecommerce.auth_service.service;

import com.ecommerce.auth_service.dto.request.AddressRequest;
import com.ecommerce.auth_service.dto.request.AddressUpdateRequest;
import com.ecommerce.auth_service.dto.response.AddressResponse;
import com.ecommerce.auth_service.entity.Address;
import com.ecommerce.auth_service.entity.User;
import com.ecommerce.auth_service.exception.BadRequestException;
import com.ecommerce.auth_service.exception.ResourceNotFoundException;
import com.ecommerce.auth_service.repository.AddressRepository;
import com.ecommerce.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional
    public AddressResponse addAddress(UUID userId, AddressRequest request) {
        User user = findUserOrThrow(userId);
        String normalizedLine2 = StringUtils.hasText(request.getAddressLine2())
                ? request.getAddressLine2().trim()
                : null;

        Optional<Address> existing = (normalizedLine2 == null)
                ? addressRepository.findDuplicateWithoutLine2(
                        userId,
                        request.getAddressLine1().trim(),
                        request.getCity().trim(),
                        request.getState().trim(),
                        request.getCountry().trim(),
                        request.getZipCode().trim())
                : addressRepository.findDuplicateWithLine2(
                        userId,
                        request.getAddressLine1().trim(),
                        request.getCity().trim(),
                        request.getState().trim(),
                        request.getCountry().trim(),
                        request.getZipCode().trim(),
                        normalizedLine2);
        if (existing.isPresent()) {
            log.info("Duplicate address request for user: {} — returning existing address", userId);
            return AddressResponse.from(existing.get());
        }

        boolean hasExistingAddresses = addressRepository.existsByUserUserId(userId);
        boolean isDefault = !hasExistingAddresses || request.isDefault();

        if (isDefault && hasExistingAddresses) {
            addressRepository.clearDefaultForUser(userId);
        }
        Address address = new Address();
        address.setUser(user);
        address.setAddressLine1(request.getAddressLine1().trim());
        address.setAddressLine2(normalizedLine2);
        address.setCity(request.getCity().trim());
        address.setState(request.getState().trim());
        address.setCountry(request.getCountry().trim());
        address.setZipCode(request.getZipCode().trim());
        address.setDefault(request.isDefault());

        Address saved = addressRepository.save(address);
        log.info("Address added for user: {}", userId);
        return AddressResponse.from(saved);
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public AddressResponse updateAddress(UUID userId, UUID addressId, AddressUpdateRequest request) {
        Address address = addressRepository.findByAddressIdAndUserUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (StringUtils.hasText(request.getAddressLine1())) {
            address.setAddressLine1(request.getAddressLine1().trim().toLowerCase());
        }
        if (request.getAddressLine2() != null) {
            address.setAddressLine2(
                    StringUtils.hasText(request.getAddressLine2())
                            ? request.getAddressLine2().trim().toLowerCase()
                            : null
            );
        }
        if (StringUtils.hasText(request.getCity())) {
            address.setCity(request.getCity().trim().toLowerCase());
        }
        if (StringUtils.hasText(request.getState())) {
            address.setState(request.getState().trim().toLowerCase());
        }
        if (StringUtils.hasText(request.getCountry())) {
            address.setCountry(request.getCountry().trim().toLowerCase());
        }
        if (StringUtils.hasText(request.getZipCode())) {
            address.setZipCode(request.getZipCode().trim());
        }
        if (Boolean.TRUE.equals(request.getIsDefault()) && !address.isDefault()) {
            addressRepository.clearDefaultForUser(userId);
            address.setDefault(true);

        } else if (Boolean.FALSE.equals(request.getIsDefault()) && address.isDefault()) {
            throw new BadRequestException("At least one default address is required");
        }

        Address saved = addressRepository.save(address);
        log.info("Address {} updated for user: {}", addressId, userId);
        return AddressResponse.from(saved);
    }


    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses(UUID userId) {
        List<AddressResponse> addresses = addressRepository.findAllByUserIdOrdered(userId)
                .stream()
                .map(AddressResponse::from)
                .toList();
        log.debug("Fetched {} address(es) for user: {}", addresses.size(), userId);
        return addresses;
    }
}
