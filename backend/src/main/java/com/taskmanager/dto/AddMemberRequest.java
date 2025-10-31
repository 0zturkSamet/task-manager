package com.taskmanager.dto;

import com.taskmanager.entity.ProjectRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddMemberRequest {

    private UUID userId;

    @Email(message = "Email must be valid")
    private String email;

    @NotNull(message = "Role is required")
    private ProjectRole role;
}
