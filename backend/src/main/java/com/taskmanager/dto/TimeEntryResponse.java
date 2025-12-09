package com.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for time entry data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeEntryResponse {

    private UUID id;
    private UUID userId;
    private UUID taskId;

    // Task information (if linked to a task)
    private String taskTitle;
    private String projectName;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private String description;
    private Boolean isManual;
    private Boolean isRunning;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Derived fields for convenience
    private Double durationHours;
    private String durationFormatted; // e.g., "2h 30m"

    /**
     * Helper method to check if timer is running
     */
    public Boolean getIsRunning() {
        return endTime == null;
    }

    /**
     * Helper method to get duration in hours
     */
    public Double getDurationHours() {
        if (durationMinutes == null) {
            return 0.0;
        }
        return Math.round(durationMinutes / 60.0 * 100.0) / 100.0; // Round to 2 decimal places
    }

    /**
     * Helper method to format duration as "Xh Ym"
     */
    public String getDurationFormatted() {
        if (durationMinutes == null || durationMinutes == 0) {
            return "0m";
        }
        int hours = durationMinutes / 60;
        int minutes = durationMinutes % 60;

        if (hours == 0) {
            return minutes + "m";
        } else if (minutes == 0) {
            return hours + "h";
        } else {
            return hours + "h " + minutes + "m";
        }
    }
}
