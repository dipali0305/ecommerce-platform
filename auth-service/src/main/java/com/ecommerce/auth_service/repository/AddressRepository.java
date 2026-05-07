package com.ecommerce.auth_service.repository;

import com.ecommerce.auth_service.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    boolean existsByUserUserId(UUID userId);

    @Query("""
            SELECT a FROM Address a
            WHERE a.user.userId = :userId
            ORDER BY a.isDefault DESC, a.createdAt DESC
            """)
    List<Address> findAllByUserIdOrdered(@Param("userId") UUID userId);

    Optional<Address> findByAddressIdAndUserUserId(UUID addressId, UUID userId);

    @Query("""
            SELECT a FROM Address a
            WHERE a.user.userId    = :userId
              AND LOWER(a.addressLine1) = LOWER(:addressLine1)
              AND LOWER(a.city)         = LOWER(:city)
              AND LOWER(a.state)        = LOWER(:state)
              AND LOWER(a.country)      = LOWER(:country)
              AND a.zipCode             = :zipCode
              AND a.addressLine2 IS NULL
            """)
    Optional<Address> findDuplicateWithoutLine2(
            @Param("userId") UUID userId,
            @Param("addressLine1") String addressLine1,
            @Param("city") String city,
            @Param("state") String state,
            @Param("country") String country,
            @Param("zipCode") String zipCode
    );

    @Query("""
            SELECT a FROM Address a
            WHERE a.user.userId    = :userId
              AND LOWER(a.addressLine1)  = LOWER(:addressLine1)
              AND LOWER(a.city)          = LOWER(:city)
              AND LOWER(a.state)         = LOWER(:state)
              AND LOWER(a.country)       = LOWER(:country)
              AND a.zipCode              = :zipCode
              AND LOWER(a.addressLine2)  = LOWER(:addressLine2)
            """)
    Optional<Address> findDuplicateWithLine2(
            @Param("userId") UUID userId,
            @Param("addressLine1") String addressLine1,
            @Param("city") String city,
            @Param("state") String state,
            @Param("country") String country,
            @Param("zipCode") String zipCode,
            @Param("addressLine2") String addressLine2
    );

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.userId = :userId")
    void clearDefaultForUser(@Param("userId") UUID userId);
}
