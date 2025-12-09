package com.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request DTO for updating an existing time entry
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTimeEntryRequest {

    /**
     * Optional task ID to link/relink this time entry to
     */
    private UUID taskId;

    /**
     * Optional new start time
     */
    private LocalDateTime startTime;

    /**
     * Optional new end time
     */
    private LocalDateTime endTime;

    /**
     * Optional new description
     */
    private String description;
}
