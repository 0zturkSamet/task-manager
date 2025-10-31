package com.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskStatisticsResponse {

    // Task counts by status
    private Long totalTasks;
    private Long todoCount;
    private Long inProgressCount;
    private Long inReviewCount;
    private Long doneCount;
    private Long cancelledCount;

    // Task counts by priority
    private Long lowPriorityCount;
    private Long mediumPriorityCount;
    private Long highPriorityCount;
    private Long urgentPriorityCount;

    // Time-based statistics
    private Long overdueCount;
    private Long dueTodayCount;
    private Long dueThisWeekCount;

    // Assignment statistics
    private Long unassignedCount;
    private Long assignedToMeCount;

    // Completion metrics
    private BigDecimal completionRate;  // Percentage of completed tasks
    private BigDecimal onTimeCompletionRate;  // Percentage completed before due date

    // Time tracking
    private BigDecimal totalEstimatedHours;
    private BigDecimal totalActualHours;
}
