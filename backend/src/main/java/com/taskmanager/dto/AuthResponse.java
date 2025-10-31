package com.taskmanager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String profileImage;
    private String token;

    @JsonProperty("expiresIn")
    private Long expiresIn;
}
