package com.taskmanager.repository;

import com.taskmanager.entity.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    /**
     * Find the currently running timer for a user (where end_time is NULL)
     */
    Optional<TimeEntry> findByUserIdAndEndTimeIsNull(UUID userId);

    /**
     * Find all time entries for a user, ordered by start time (most recent first)
     */
    List<TimeEntry> findByUserIdOrderByStartTimeDesc(UUID userId);

    /**
     * Find all time entries for a specific task
     */
    List<TimeEntry> findByTaskIdOrderByStartTimeDesc(UUID taskId);

    /**
     * Find all time entries for a user within a date range
     */
    @Query("""
        SELECT te FROM TimeEntry te
        WHERE te.userId = :userId
        AND te.startTime >= :startDate
        AND te.startTime <= :endDate
        ORDER BY te.startTime DESC
        """)
    List<TimeEntry> findByUserIdAndDateRange(
        @Param("userId") UUID userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find all time entries for a task within a date range
     */
    @Query("""
        SELECT te FROM TimeEntry te
        WHERE te.taskId = :taskId
        AND te.startTime >= :startDate
        AND te.startTime <= :endDate
        ORDER BY te.startTime DESC
        """)
    List<TimeEntry> findByTaskIdAndDateRange(
        @Param("taskId") UUID taskId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find all completed time entries (with end_time set) for a user
     */
    @Query("""
        SELECT te FROM TimeEntry te
        WHERE te.userId = :userId
        AND te.endTime IS NOT NULL
        ORDER BY te.startTime DESC
        """)
    List<TimeEntry> findCompletedEntriesByUserId(@Param("userId") UUID userId);

    /**
     * Calculate total hours logged by a user within a date range
     */
    @Query("""
        SELECT COALESCE(SUM(te.durationMinutes), 0) / 60.0
        FROM TimeEntry te
        WHERE te.userId = :userId
        AND te.endTime IS NOT NULL
        AND te.startTime >= :startDate
        AND te.startTime <= :endDate
        """)
    Double calculateTotalHoursByUserAndDateRange(
        @Param("userId") UUID userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Calculate total hours logged for a specific task
     */
    @Query("""
        SELECT COALESCE(SUM(te.durationMinutes), 0) / 60.0
        FROM TimeEntry te
        WHERE te.taskId = :taskId
        AND te.endTime IS NOT NULL
        """)
    Double calculateTotalHoursByTask(@Param("taskId") UUID taskId);

    /**
     * Count time entries for a task
     */
    Long countByTaskId(UUID taskId);

    /**
     * Find all time entries for a user on a specific date
     */
    @Query("""
        SELECT te FROM TimeEntry te
        WHERE te.userId = :userId
        AND DATE(te.startTime) = DATE(:date)
        ORDER BY te.startTime DESC
        """)
    List<TimeEntry> findByUserIdAndDate(
        @Param("userId") UUID userId,
        @Param("date") LocalDateTime date
    );

    /**
     * Check if there are any overlapping time entries for a user
     * (Used to prevent manual entries that overlap with existing entries)
     */
    @Query("""
        SELECT COUNT(te) > 0
        FROM TimeEntry te
        WHERE te.userId = :userId
        AND te.id != :excludeId
        AND (
            (te.startTime <= :startTime AND (te.endTime IS NULL OR te.endTime > :startTime))
            OR
            (te.startTime < :endTime AND (te.endTime IS NULL OR te.endTime >= :endTime))
            OR
            (:startTime <= te.startTime AND :endTime >= te.startTime)
        )
        """)
    Boolean hasOverlappingEntries(
        @Param("userId") UUID userId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("excludeId") UUID excludeId
    );

    /**
     * Get daily time summary for a user within a date range
     * Returns date and total hours grouped by day
     */
    @Query("""
        SELECT DATE(te.startTime) as date, COALESCE(SUM(te.durationMinutes), 0) / 60.0 as hours
        FROM TimeEntry te
        WHERE te.userId = :userId
        AND te.endTime IS NOT NULL
        AND te.startTime >= :startDate
        AND te.startTime <= :endDate
        GROUP BY DATE(te.startTime)
        ORDER BY DATE(te.startTime) DESC
        """)
    List<Object[]> getDailySummary(
        @Param("userId") UUID userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Delete all time entries for a task (cascade when task is deleted)
     */
    void deleteByTaskId(UUID taskId);
}
