package com.ecommerce.auth_service.repository;

import com.ecommerce.auth_service.entity.Role;
import com.ecommerce.auth_service.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByRoleName(RoleName roleName);
}
