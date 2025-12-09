package com.taskmanager.service;

import com.taskmanager.dto.*;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TimeEntry;
import com.taskmanager.entity.User;
import com.taskmanager.exception.BadRequestException;
import com.taskmanager.exception.ForbiddenException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.TimeEntryRepository;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeTrackerService {

    private final TimeEntryRepository timeEntryRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final WorkingHoursService workingHoursService;

    /**
     * Start a new timer for the user
     * If there's already a running timer, it will be stopped first
     */
    @Transactional
    public TimeEntryResponse startTimer(UUID userId, StartTimerRequest request) {
        log.info("Starting timer for user ID: {}", userId);

        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // If task is specified, verify it exists and user has access
        if (request.getTaskId() != null) {
            Task task = taskRepository.findByIdAndIsActiveTrue(request.getTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

            // Check if user has access to the task
            if (!taskRepository.hasAccessToTask(request.getTaskId(), userId)) {
                throw new ForbiddenException("You don't have access to this task");
            }
        }

        // Stop any existing running timer
        timeEntryRepository.findByUserIdAndEndTimeIsNull(userId)
                .ifPresent(runningTimer -> {
                    log.info("Stopping existing running timer ID: {}", runningTimer.getId());
                    stopTimerInternal(runningTimer);
                });

        // Create new time entry
        TimeEntry timeEntry = TimeEntry.builder()
                .userId(userId)
                .taskId(request.getTaskId())
                .startTime(LocalDateTime.now())
                .description(request.getDescription())
                .isManual(false)
                .build();

        TimeEntry savedEntry = timeEntryRepository.save(timeEntry);
        log.info("Timer started successfully with ID: {}", savedEntry.getId());

        return mapToTimeEntryResponse(savedEntry);
    }

    /**
     * Stop the currently running timer for the user
     */
    @Transactional
    public TimeEntryResponse stopTimer(UUID userId, StopTimerRequest request) {
        log.info("Stopping timer for user ID: {}", userId);

        // Find running timer
        TimeEntry timeEntry = timeEntryRepository.findByUserIdAndEndTimeIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No active timer found"));

        // Update description if provided
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            timeEntry.setDescription(request.getDescription());
        }

        // Stop the timer
        stopTimerInternal(timeEntry);

        TimeEntry stoppedEntry = timeEntryRepository.save(timeEntry);
        log.info("Timer stopped successfully. Duration: {} minutes", stoppedEntry.getDurationMinutes());

        return mapToTimeEntryResponse(stoppedEntry);
    }

    /**
     * Internal method to stop a timer and update task actual hours
     */
    private void stopTimerInternal(TimeEntry timeEntry) {
        timeEntry.setEndTime(LocalDateTime.now());

        // Calculate duration in minutes
        Duration duration = Duration.between(timeEntry.getStartTime(), timeEntry.getEndTime());
        int durationMinutes = (int) duration.toMinutes();
        timeEntry.setDurationMinutes(durationMinutes);

        // Update task actual hours if linked to a task
        if (timeEntry.getTaskId() != null) {
            taskRepository.findByIdAndIsActiveTrue(timeEntry.getTaskId())
                    .ifPresent(task -> {
                        BigDecimal hoursToAdd = BigDecimal.valueOf(durationMinutes)
                                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

                        BigDecimal currentActualHours = task.getActualHours() != null
                                ? task.getActualHours()
                                : BigDecimal.ZERO;

                        task.setActualHours(currentActualHours.add(hoursToAdd));
                        taskRepository.save(task);
                        log.info("Updated task {} actual hours to {}", task.getId(), task.getActualHours());
                    });
        }
    }

    /**
     * Get the currently running timer for the user
     */
    @Transactional(readOnly = true)
    public TimeEntryResponse getActiveTimer(UUID userId) {
        log.info("Fetching active timer for user ID: {}", userId);

        return timeEntryRepository.findByUserIdAndEndTimeIsNull(userId)
                .map(this::mapToTimeEntryResponse)
                .orElse(null);
    }

    /**
     * Create a manual time entry (not from timer)
     */
    @Transactional
    public TimeEntryResponse createTimeEntry(UUID userId, CreateTimeEntryRequest request) {
        log.info("Creating manual time entry for user ID: {}", userId);

        // Validate time range
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        // Check for overlapping entries
        UUID excludeId = UUID.randomUUID(); // Dummy UUID for new entries
        if (timeEntryRepository.hasOverlappingEntries(userId, request.getStartTime(),
                request.getEndTime(), excludeId)) {
            throw new BadRequestException("This time entry overlaps with an existing entry");
        }

        // If task is specified, verify it exists and user has access
        if (request.getTaskId() != null) {
            taskRepository.findByIdAndIsActiveTrue(request.getTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

            if (!taskRepository.hasAccessToTask(request.getTaskId(), userId)) {
                throw new ForbiddenException("You don't have access to this task");
            }
        }

        // Calculate duration
        Duration duration = Duration.between(request.getStartTime(), request.getEndTime());
        int durationMinutes = (int) duration.toMinutes();

        // Create time entry
        TimeEntry timeEntry = TimeEntry.builder()
                .userId(userId)
                .taskId(request.getTaskId())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .durationMinutes(durationMinutes)
                .description(request.getDescription())
                .isManual(true)
                .build();

        TimeEntry savedEntry = timeEntryRepository.save(timeEntry);

        // Update task actual hours if linked to a task
        if (savedEntry.getTaskId() != null) {
            updateTaskActualHours(savedEntry.getTaskId(), BigDecimal.valueOf(durationMinutes / 60.0));
        }

        log.info("Manual time entry created successfully with ID: {}", savedEntry.getId());
        return mapToTimeEntryResponse(savedEntry);
    }

    /**
     * Update an existing time entry
     */
    @Transactional
    public TimeEntryResponse updateTimeEntry(UUID userId, UUID entryId, UpdateTimeEntryRequest request) {
        log.info("Updating time entry ID: {} for user ID: {}", entryId, userId);

        // Find time entry and verify ownership
        TimeEntry timeEntry = timeEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Time entry not found"));

        if (!timeEntry.getUserId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to update this time entry");
        }

        // Store old values for recalculation
        UUID oldTaskId = timeEntry.getTaskId();
        Integer oldDurationMinutes = timeEntry.getDurationMinutes();

        // Update fields if provided
        if (request.getTaskId() != null) {
            if (!taskRepository.hasAccessToTask(request.getTaskId(), userId)) {
                throw new ForbiddenException("You don't have access to this task");
            }
            timeEntry.setTaskId(request.getTaskId());
        }

        if (request.getStartTime() != null) {
            timeEntry.setStartTime(request.getStartTime());
        }

        if (request.getEndTime() != null) {
            timeEntry.setEndTime(request.getEndTime());
        }

        if (request.getDescription() != null) {
            timeEntry.setDescription(request.getDescription());
        }

        // Recalculate duration if times changed
        if (timeEntry.getStartTime() != null && timeEntry.getEndTime() != null) {
            if (timeEntry.getEndTime().isBefore(timeEntry.getStartTime())) {
                throw new BadRequestException("End time must be after start time");
            }

            Duration duration = Duration.between(timeEntry.getStartTime(), timeEntry.getEndTime());
            timeEntry.setDurationMinutes((int) duration.toMinutes());
        }

        // Check for overlaps (excluding this entry)
        if (timeEntry.getStartTime() != null && timeEntry.getEndTime() != null) {
            if (timeEntryRepository.hasOverlappingEntries(userId, timeEntry.getStartTime(),
                    timeEntry.getEndTime(), entryId)) {
                throw new BadRequestException("This time entry overlaps with an existing entry");
            }
        }

        TimeEntry updatedEntry = timeEntryRepository.save(timeEntry);

        // Recalculate task actual hours
        if (oldTaskId != null && oldDurationMinutes != null) {
            // Subtract old duration from old task
            updateTaskActualHours(oldTaskId, BigDecimal.valueOf(-oldDurationMinutes / 60.0));
        }

        if (updatedEntry.getTaskId() != null && updatedEntry.getDurationMinutes() != null) {
            // Add new duration to new task
            updateTaskActualHours(updatedEntry.getTaskId(),
                    BigDecimal.valueOf(updatedEntry.getDurationMinutes() / 60.0));
        }

        log.info("Time entry updated successfully");
        return mapToTimeEntryResponse(updatedEntry);
    }

    /**
     * Delete a time entry
     */
    @Transactional
    public void deleteTimeEntry(UUID userId, UUID entryId) {
        log.info("Deleting time entry ID: {} for user ID: {}", entryId, userId);

        // Find time entry and verify ownership
        TimeEntry timeEntry = timeEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Time entry not found"));

        if (!timeEntry.getUserId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to delete this time entry");
        }

        // Subtract duration from task actual hours
        if (timeEntry.getTaskId() != null && timeEntry.getDurationMinutes() != null) {
            updateTaskActualHours(timeEntry.getTaskId(),
                    BigDecimal.valueOf(-timeEntry.getDurationMinutes() / 60.0));
        }

        timeEntryRepository.delete(timeEntry);
        log.info("Time entry deleted successfully");
    }

    /**
     * Get all time entries for a user (with optional filters)
     */
    @Transactional(readOnly = true)
    public List<TimeEntryResponse> getTimeEntries(UUID userId, UUID taskId, LocalDateTime startDate,
                                                    LocalDateTime endDate) {
        log.info("Fetching time entries for user ID: {}", userId);

        List<TimeEntry> entries;

        if (taskId != null) {
            // Verify user has access to task
            if (!taskRepository.hasAccessToTask(taskId, userId)) {
                throw new ForbiddenException("You don't have access to this task");
            }

            if (startDate != null && endDate != null) {
                entries = timeEntryRepository.findByTaskIdAndDateRange(taskId, startDate, endDate);
            } else {
                entries = timeEntryRepository.findByTaskIdOrderByStartTimeDesc(taskId);
            }
        } else if (startDate != null && endDate != null) {
            entries = timeEntryRepository.findByUserIdAndDateRange(userId, startDate, endDate);
        } else {
            entries = timeEntryRepository.findByUserIdOrderByStartTimeDesc(userId);
        }

        return entries.stream()
                .map(this::mapToTimeEntryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get time summary for a user within a date range
     */
    @Transactional(readOnly = true)
    public TimeSummaryResponse getTimeSummary(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching time summary for user ID: {} from {} to {}", userId, startDate, endDate);

        List<TimeEntry> entries = timeEntryRepository.findByUserIdAndDateRange(userId, startDate, endDate);

        // Filter only completed entries
        List<TimeEntry> completedEntries = entries.stream()
                .filter(e -> e.getEndTime() != null)
                .collect(Collectors.toList());

        // Calculate total hours
        double totalHours = completedEntries.stream()
                .mapToInt(TimeEntry::getDurationMinutes)
                .sum() / 60.0;

        // Group by date
        List<TimeSummaryResponse.DailySummary> dailySummaries = completedEntries.stream()
                .collect(Collectors.groupingBy(e -> e.getStartTime().toLocalDate()))
                .entrySet().stream()
                .map(entry -> {
                    double hours = entry.getValue().stream()
                            .mapToInt(TimeEntry::getDurationMinutes)
                            .sum() / 60.0;
                    return TimeSummaryResponse.DailySummary.builder()
                            .date(entry.getKey())
                            .hours(hours)
                            .entryCount(entry.getValue().size())
                            .build();
                })
                .collect(Collectors.toList());

        // Group by task
        List<TimeSummaryResponse.TaskTimeSummary> taskSummaries = completedEntries.stream()
                .filter(e -> e.getTaskId() != null)
                .collect(Collectors.groupingBy(TimeEntry::getTaskId))
                .entrySet().stream()
                .map(entry -> {
                    UUID taskId = entry.getKey();
                    Task task = taskRepository.findById(taskId).orElse(null);

                    double hours = entry.getValue().stream()
                            .mapToInt(TimeEntry::getDurationMinutes)
                            .sum() / 60.0;

                    Double estimatedHours = task != null && task.getEstimatedHours() != null
                            ? task.getEstimatedHours().doubleValue()
                            : null;

                    Double variance = estimatedHours != null ? estimatedHours - hours : null;

                    return TimeSummaryResponse.TaskTimeSummary.builder()
                            .taskId(taskId.toString())
                            .taskTitle(task != null ? task.getTitle() : "Unknown")
                            .hours(hours)
                            .estimatedHours(estimatedHours)
                            .variance(variance)
                            .entryCount(entry.getValue().size())
                            .build();
                })
                .collect(Collectors.toList());

        return TimeSummaryResponse.builder()
                .startDate(startDate.toLocalDate())
                .endDate(endDate.toLocalDate())
                .totalHours(totalHours)
                .totalEntries(completedEntries.size())
                .dailySummaries(dailySummaries)
                .taskSummaries(taskSummaries)
                .build();
    }

    /**
     * Update task actual hours (add or subtract)
     */
    private void updateTaskActualHours(UUID taskId, BigDecimal hoursChange) {
        taskRepository.findByIdAndIsActiveTrue(taskId).ifPresent(task -> {
            BigDecimal currentActualHours = task.getActualHours() != null
                    ? task.getActualHours()
                    : BigDecimal.ZERO;

            BigDecimal newActualHours = currentActualHours.add(hoursChange)
                    .max(BigDecimal.ZERO) // Ensure it doesn't go negative
                    .setScale(2, RoundingMode.HALF_UP);

            task.setActualHours(newActualHours);
            taskRepository.save(task);
        });
    }

    /**
     * Map TimeEntry entity to TimeEntryResponse DTO
     */
    private TimeEntryResponse mapToTimeEntryResponse(TimeEntry entry) {
        String taskTitle = null;
        String projectName = null;

        if (entry.getTask() != null) {
            taskTitle = entry.getTask().getTitle();
            // Get project name if task has project
            if (entry.getTask().getProjectId() != null) {
                projectName = taskRepository.findById(entry.getTask().getId())
                        .map(task -> task.getProjectId().toString())
                        .orElse(null);
            }
        }

        return TimeEntryResponse.builder()
                .id(entry.getId())
                .userId(entry.getUserId())
                .taskId(entry.getTaskId())
                .taskTitle(taskTitle)
                .projectName(projectName)
                .startTime(entry.getStartTime())
                .endTime(entry.getEndTime())
                .durationMinutes(entry.getDurationMinutes())
                .description(entry.getDescription())
                .isManual(entry.getIsManual())
                .isRunning(entry.isRunning())
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .build();
    }

    /**
     * Get overtime report for a user within a date range
     */
    @Transactional(readOnly = true)
    public OvertimeReportResponse getOvertimeReport(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching overtime report for user ID: {} from {} to {}", userId, startDate, endDate);

        // Get user's working hours configuration
        WorkingHoursConfigDTO config = workingHoursService.getOrCreateConfig(userId.toString());
        List<String> workingDays = config.getWorkingDays();

        // Get all time entries in the date range
        List<TimeEntry> entries = timeEntryRepository.findByUserIdAndDateRange(userId, startDate, endDate);
        List<TimeEntry> completedEntries = entries.stream()
                .filter(e -> e.getEndTime() != null)
                .collect(Collectors.toList());

        // Group by date and calculate daily overtime
        List<OvertimeReportResponse.DailyOvertime> dailyOvertimes = completedEntries.stream()
                .collect(Collectors.groupingBy(e -> e.getStartTime().toLocalDate()))
                .entrySet().stream()
                .map(entry -> {
                    java.time.LocalDate date = entry.getKey();
                    String dayOfWeek = date.getDayOfWeek().name();
                    boolean isWorkingDay = workingDays.contains(dayOfWeek);

                    double actualHours = entry.getValue().stream()
                            .mapToInt(TimeEntry::getDurationMinutes)
                            .sum() / 60.0;

                    BigDecimal expectedHours = isWorkingDay ? config.getExpectedHoursPerDay() : BigDecimal.ZERO;
                    BigDecimal actualHoursDecimal = BigDecimal.valueOf(actualHours).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal overtimeHours = actualHoursDecimal.subtract(expectedHours).max(BigDecimal.ZERO);

                    return OvertimeReportResponse.DailyOvertime.builder()
                            .date(date)
                            .dayOfWeek(dayOfWeek)
                            .expectedHours(expectedHours)
                            .actualHours(actualHoursDecimal)
                            .overtimeHours(overtimeHours)
                            .isWorkingDay(isWorkingDay)
                            .build();
                })
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());

        // Calculate total overtime
        BigDecimal totalOvertimeHours = dailyOvertimes.stream()
                .map(OvertimeReportResponse.DailyOvertime::getOvertimeHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate weekly summary (last 7 days from end date)
        java.time.LocalDate weekStart = endDate.toLocalDate().minusDays(6);
        BigDecimal actualWeekHours = dailyOvertimes.stream()
                .filter(d -> !d.getDate().isBefore(weekStart))
                .map(OvertimeReportResponse.DailyOvertime::getActualHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal weeklyOvertime = actualWeekHours.subtract(config.getExpectedHoursPerWeek()).max(BigDecimal.ZERO);

        OvertimeReportResponse.WeeklySummary weeklySummary = OvertimeReportResponse.WeeklySummary.builder()
                .expectedHoursPerWeek(config.getExpectedHoursPerWeek())
                .actualHoursThisWeek(actualWeekHours)
                .overtimeHours(weeklyOvertime)
                .build();

        return OvertimeReportResponse.builder()
                .startDate(startDate.toLocalDate())
                .endDate(endDate.toLocalDate())
                .totalOvertimeHours(totalOvertimeHours)
                .dailyOvertimes(dailyOvertimes)
                .weeklySummary(weeklySummary)
                .build();
    }

    /**
     * Get missed hours report for a user within a date range
     */
    @Transactional(readOnly = true)
    public MissedHoursReportResponse getMissedHoursReport(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching missed hours report for user ID: {} from {} to {}", userId, startDate, endDate);

        // Get user's working hours configuration
        WorkingHoursConfigDTO config = workingHoursService.getOrCreateConfig(userId.toString());
        List<String> workingDays = config.getWorkingDays();

        // Get all time entries in the date range
        List<TimeEntry> entries = timeEntryRepository.findByUserIdAndDateRange(userId, startDate, endDate);
        List<TimeEntry> completedEntries = entries.stream()
                .filter(e -> e.getEndTime() != null)
                .collect(Collectors.toList());

        // Group by date
        var entriesByDate = completedEntries.stream()
                .collect(Collectors.groupingBy(e -> e.getStartTime().toLocalDate()));

        // Generate daily missed hours for all working days in the range
        long daysBetween = java.time.temporal.ChronoUnit.DAYS
                .between(startDate.toLocalDate(), endDate.toLocalDate().plusDays(1));
        List<MissedHoursReportResponse.DailyMissedHours> dailyMissedHours = daysBetween > 0 ?
                java.util.stream.IntStream.range(0, (int) daysBetween)
                .mapToObj(i -> startDate.toLocalDate().plusDays(i))
                .map(date -> {
                    String dayOfWeek = date.getDayOfWeek().name();
                    boolean isWorkingDay = workingDays.contains(dayOfWeek);

                    double actualHours = entriesByDate.getOrDefault(date, List.of()).stream()
                            .mapToInt(TimeEntry::getDurationMinutes)
                            .sum() / 60.0;

                    BigDecimal expectedHours = isWorkingDay ? config.getExpectedHoursPerDay() : BigDecimal.ZERO;
                    BigDecimal actualHoursDecimal = BigDecimal.valueOf(actualHours).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal missedHours = expectedHours.subtract(actualHoursDecimal).max(BigDecimal.ZERO);

                    return MissedHoursReportResponse.DailyMissedHours.builder()
                            .date(date)
                            .dayOfWeek(dayOfWeek)
                            .expectedHours(expectedHours)
                            .actualHours(actualHoursDecimal)
                            .missedHours(missedHours)
                            .isWorkingDay(isWorkingDay)
                            .build();
                })
                .collect(Collectors.toList()) : List.of();

        // Calculate total missed hours
        BigDecimal totalMissedHours = dailyMissedHours.stream()
                .map(MissedHoursReportResponse.DailyMissedHours::getMissedHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Count days with missed hours
        long daysWithMissedHours = dailyMissedHours.stream()
                .filter(d -> d.getMissedHours().compareTo(BigDecimal.ZERO) > 0)
                .count();

        // Calculate weekly summary (last 7 days from end date)
        java.time.LocalDate weekStart = endDate.toLocalDate().minusDays(6);
        BigDecimal actualWeekHours = dailyMissedHours.stream()
                .filter(d -> !d.getDate().isBefore(weekStart))
                .map(MissedHoursReportResponse.DailyMissedHours::getActualHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal weeklyMissed = config.getExpectedHoursPerWeek().subtract(actualWeekHours).max(BigDecimal.ZERO);

        MissedHoursReportResponse.WeeklySummary weeklySummary = MissedHoursReportResponse.WeeklySummary.builder()
                .expectedHoursPerWeek(config.getExpectedHoursPerWeek())
                .actualHoursThisWeek(actualWeekHours)
                .missedHours(weeklyMissed)
                .build();

        return MissedHoursReportResponse.builder()
                .startDate(startDate.toLocalDate())
                .endDate(endDate.toLocalDate())
                .totalMissedHours(totalMissedHours)
                .daysWithMissedHours((int) daysWithMissedHours)
                .dailyMissedHours(dailyMissedHours)
                .weeklySummary(weeklySummary)
                .build();
    }
}
