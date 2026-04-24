package com.ecommerce.auth_service.dto.request;

import com.ecommerce.auth_service.enums.RoleName;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleUpdateRequest {

    @JsonProperty("role_name")
    @NotBlank(message = "Role name is required")
    private RoleName roleName;
}
