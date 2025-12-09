package com.taskmanager.controller;

import com.taskmanager.dto.*;
import com.taskmanager.entity.User;
import com.taskmanager.service.TimeTrackerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Time Tracker", description = "Endpoints for time tracking and timer management")
@SecurityRequirement(name = "Bearer Authentication")
public class TimeTrackerController {

    private final TimeTrackerService timeTrackerService;

    // ===== Timer Management Endpoints =====

    @PostMapping("/timer/start")
    @Operation(summary = "Start timer", description = "Starts a new timer for the authenticated user. If a timer is already running, it will be stopped first.")
    public ResponseEntity<TimeEntryResponse> startTimer(
            @AuthenticationPrincipal User user,
            @RequestBody StartTimerRequest request
    ) {
        TimeEntryResponse response = timeTrackerService.startTimer(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/timer/stop")
    @Operation(summary = "Stop timer", description = "Stops the currently running timer for the authenticated user")
    public ResponseEntity<TimeEntryResponse> stopTimer(
            @AuthenticationPrincipal User user,
            @RequestBody(required = false) StopTimerRequest request
    ) {
        StopTimerRequest stopRequest = request != null ? request : new StopTimerRequest();
        TimeEntryResponse response = timeTrackerService.stopTimer(user.getId(), stopRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/timer/active")
    @Operation(summary = "Get active timer", description = "Returns the currently running timer for the authenticated user, or null if no timer is running")
    public ResponseEntity<Map<String, Object>> getActiveTimer(
            @AuthenticationPrincipal User user
    ) {
        TimeEntryResponse activeTimer = timeTrackerService.getActiveTimer(user.getId());
        return ResponseEntity.ok(Map.of("activeTimer", activeTimer != null ? activeTimer : "null"));
    }

    // ===== Time Entry CRUD Endpoints =====

    @PostMapping("/time-entries")
    @Operation(summary = "Create manual time entry", description = "Creates a manual time entry (not from timer). Validates against overlapping entries.")
    public ResponseEntity<TimeEntryResponse> createTimeEntry(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateTimeEntryRequest request
    ) {
        TimeEntryResponse response = timeTrackerService.createTimeEntry(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/time-entries")
    @Operation(summary = "Get time entries", description = "Returns time entries for the authenticated user with optional filters")
    public ResponseEntity<List<TimeEntryResponse>> getTimeEntries(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Filter by task ID") @RequestParam(required = false) UUID taskId,
            @Parameter(description = "Start date (ISO format)") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        List<TimeEntryResponse> entries = timeTrackerService.getTimeEntries(
                user.getId(), taskId, startDate, endDate);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/time-entries/{entryId}")
    @Operation(summary = "Get time entry by ID", description = "Returns a specific time entry")
    public ResponseEntity<TimeEntryResponse> getTimeEntryById(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Time entry ID") @PathVariable UUID entryId
    ) {
        // This would need a getById method in the service (not implemented in this version)
        // For now, we can fetch all entries and filter
        List<TimeEntryResponse> entries = timeTrackerService.getTimeEntries(user.getId(), null, null, null);
        TimeEntryResponse entry = entries.stream()
                .filter(e -> e.getId().equals(entryId))
                .findFirst()
                .orElse(null);

        if (entry == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(entry);
    }

    @PutMapping("/time-entries/{entryId}")
    @Operation(summary = "Update time entry", description = "Updates an existing time entry. Validates ownership and time ranges.")
    public ResponseEntity<TimeEntryResponse> updateTimeEntry(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Time entry ID") @PathVariable UUID entryId,
            @Valid @RequestBody UpdateTimeEntryRequest request
    ) {
        TimeEntryResponse response = timeTrackerService.updateTimeEntry(user.getId(), entryId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/time-entries/{entryId}")
    @Operation(summary = "Delete time entry", description = "Deletes a time entry and updates associated task actual hours")
    public ResponseEntity<?> deleteTimeEntry(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Time entry ID") @PathVariable UUID entryId
    ) {
        timeTrackerService.deleteTimeEntry(user.getId(), entryId);
        return ResponseEntity.ok(Map.of("message", "Time entry deleted successfully"));
    }

    // ===== Reports & Analytics Endpoints =====

    @GetMapping("/reports/time-summary")
    @Operation(summary = "Get time summary", description = "Returns time tracking summary with daily and task breakdowns for a date range")
    public ResponseEntity<TimeSummaryResponse> getTimeSummary(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Start date (ISO format)", required = true) @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)", required = true) @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        TimeSummaryResponse response = timeTrackerService.getTimeSummary(user.getId(), startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks/{taskId}/time-summary")
    @Operation(summary = "Get task time summary", description = "Returns time tracking summary for a specific task")
    public ResponseEntity<Map<String, Object>> getTaskTimeSummary(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Task ID") @PathVariable UUID taskId
    ) {
        // Get all time entries for this task
        List<TimeEntryResponse> entries = timeTrackerService.getTimeEntries(user.getId(), taskId, null, null);

        // Calculate totals
        double totalHours = entries.stream()
                .filter(e -> e.getDurationMinutes() != null)
                .mapToDouble(TimeEntryResponse::getDurationHours)
                .sum();

        Map<String, Object> summary = Map.of(
                "taskId", taskId,
                "totalHours", totalHours,
                "entryCount", entries.size(),
                "entries", entries
        );

        return ResponseEntity.ok(summary);
    }

    // ===== Quick Stats Endpoints =====

    @GetMapping("/time-tracking/today")
    @Operation(summary = "Get today's time", description = "Returns total hours logged today")
    public ResponseEntity<Map<String, Object>> getTodayTime(
            @AuthenticationPrincipal User user
    ) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        List<TimeEntryResponse> entries = timeTrackerService.getTimeEntries(
                user.getId(), null, startOfDay, endOfDay);

        double totalHours = entries.stream()
                .filter(e -> e.getDurationMinutes() != null)
                .mapToDouble(TimeEntryResponse::getDurationHours)
                .sum();

        return ResponseEntity.ok(Map.of(
                "date", startOfDay.toLocalDate(),
                "totalHours", totalHours,
                "entryCount", entries.size()
        ));
    }

    @GetMapping("/time-tracking/week")
    @Operation(summary = "Get this week's time", description = "Returns total hours logged this week")
    public ResponseEntity<Map<String, Object>> getWeekTime(
            @AuthenticationPrincipal User user
    ) {
        LocalDateTime startOfWeek = LocalDateTime.now().toLocalDate().atStartOfDay().minusDays(7);
        LocalDateTime now = LocalDateTime.now();

        List<TimeEntryResponse> entries = timeTrackerService.getTimeEntries(
                user.getId(), null, startOfWeek, now);

        double totalHours = entries.stream()
                .filter(e -> e.getDurationMinutes() != null)
                .mapToDouble(TimeEntryResponse::getDurationHours)
                .sum();

        return ResponseEntity.ok(Map.of(
                "startDate", startOfWeek.toLocalDate(),
                "endDate", now.toLocalDate(),
                "totalHours", totalHours,
                "entryCount", entries.size()
        ));
    }

    // ===== Monthly & Yearly Reports =====

    @GetMapping("/reports/monthly")
    @Operation(summary = "Get monthly report", description = "Returns time tracking summary for a specific month")
    public ResponseEntity<TimeSummaryResponse> getMonthlyReport(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Month in format YYYY-MM (e.g., 2025-01)", required = true) @RequestParam String month
    ) {
        // Parse month string to get start and end dates
        String[] parts = month.split("-");
        int year = Integer.parseInt(parts[0]);
        int monthNum = Integer.parseInt(parts[1]);

        LocalDateTime startDate = LocalDateTime.of(year, monthNum, 1, 0, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1).minusSeconds(1);

        TimeSummaryResponse response = timeTrackerService.getTimeSummary(user.getId(), startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports/yearly")
    @Operation(summary = "Get yearly report", description = "Returns time tracking summary for a specific year")
    public ResponseEntity<TimeSummaryResponse> getYearlyReport(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Year (e.g., 2025)", required = true) @RequestParam int year
    ) {
        LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        TimeSummaryResponse response = timeTrackerService.getTimeSummary(user.getId(), startDate, endDate);
        return ResponseEntity.ok(response);
    }

    // ===== Overtime & Missed Hours Endpoints =====

    @GetMapping("/reports/overtime")
    @Operation(summary = "Get overtime report", description = "Returns overtime report showing hours worked beyond expected hours")
    public ResponseEntity<OvertimeReportResponse> getOvertimeReport(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Start date (ISO format)", required = true) @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)", required = true) @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        OvertimeReportResponse response = timeTrackerService.getOvertimeReport(user.getId(), startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports/missed-hours")
    @Operation(summary = "Get missed hours report", description = "Returns missed hours report showing days with insufficient hours")
    public ResponseEntity<MissedHoursReportResponse> getMissedHoursReport(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Start date (ISO format)", required = true) @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)", required = true) @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        MissedHoursReportResponse response = timeTrackerService.getMissedHoursReport(user.getId(), startDate, endDate);
        return ResponseEntity.ok(response);
    }
}
