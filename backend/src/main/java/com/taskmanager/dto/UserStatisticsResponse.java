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
public class UserStatisticsResponse {

    // Project counts
    private Long totalProjects;
    private Long ownedProjects;
    private Long memberProjects;

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
    private Long createdByMeCount;

    // Completion metrics
    private BigDecimal completionRate;  // Percentage of completed tasks
    private BigDecimal myTasksCompletionRate;  // Completion rate for tasks assigned to me

    // Time tracking
    private BigDecimal totalEstimatedHours;
    private BigDecimal totalActualHours;
    private BigDecimal myTasksEstimatedHours;
    private BigDecimal myTasksActualHours;
}
