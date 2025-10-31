package com.taskmanager.dto;

import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskFilterRequest {

    private UUID projectId;
    private List<TaskStatus> statuses;
    private List<TaskPriority> priorities;
    private UUID assignedToId;
    private UUID createdByUserId;
    private LocalDateTime dueDateFrom;
    private LocalDateTime dueDateTo;
    private String searchText;  // Search in title and description
    private Boolean overdue;    // Filter for overdue tasks only
}
