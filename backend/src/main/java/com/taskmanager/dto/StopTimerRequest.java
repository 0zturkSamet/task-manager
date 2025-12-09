package com.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for stopping a timer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StopTimerRequest {

    /**
     * Optional description to update when stopping the timer
     * If provided, will override the description set when starting
     */
    private String description;
}
