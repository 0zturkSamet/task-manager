package com.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for starting a timer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartTimerRequest {

    /**
     * Optional task ID to link this time entry to
     * If null, the time entry will be unassigned
     */
    private UUID taskId;

    /**
     * Optional description of what work is being done
     */
    private String description;
}
