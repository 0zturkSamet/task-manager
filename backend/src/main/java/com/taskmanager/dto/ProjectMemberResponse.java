package com.taskmanager.dto;

import com.taskmanager.entity.ProjectRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberResponse {

    private UUID id;
    private UUID userId;
    private String userEmail;
    private String userName;
    private ProjectRole role;
    private LocalDateTime joinedAt;
}
