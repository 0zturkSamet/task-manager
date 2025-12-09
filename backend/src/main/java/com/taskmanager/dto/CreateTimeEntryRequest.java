package com.taskmanager.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request DTO for manually creating a time entry
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTimeEntryRequest {

    /**
     * Optional task ID to link this time entry to
     */
    private UUID taskId;

    /**
     * Start time of the time entry (required)
     */
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    /**
     * End time of the time entry (required for manual entries)
     */
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    /**
     * Optional description of work done
     */
    private String description;
}
