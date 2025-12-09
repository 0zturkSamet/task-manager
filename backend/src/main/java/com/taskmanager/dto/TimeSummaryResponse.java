package com.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for time tracking summary and reports
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSummaryResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalHours;
    private Integer totalEntries;

    // Daily breakdown
    private List<DailySummary> dailySummaries;

    // Task breakdown
    private List<TaskTimeSummary> taskSummaries;

    /**
     * Daily time summary
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailySummary {
        private LocalDate date;
        private Double hours;
        private Integer entryCount;
    }

    /**
     * Time summary by task
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskTimeSummary {
        private String taskId;
        private String taskTitle;
        private String projectName;
        private Double hours;
        private Double estimatedHours;
        private Double variance; // estimated - actual
        private Integer entryCount;
    }
}
