package com.taskmanager.dto;

import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {

    private UUID id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;

    // Project information
    private UUID projectId;
    private String projectName;

    // Assignment information
    private UUID assignedToId;
    private String assignedToName;
    private String assignedToEmail;

    // Creator information
    private UUID createdByUserId;
    private String createdByUserName;
    private String createdByUserEmail;

    // Time tracking
    private BigDecimal estimatedHours;
    private BigDecimal actualHours;

    // Dates
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Position for ordering
    private Integer position;

    // Derived fields
    private Boolean isOverdue;
    private Long commentCount;
}
