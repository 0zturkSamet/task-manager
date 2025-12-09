package com.taskmanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a time tracking entry.
 * Each entry records a period of time spent working, optionally linked to a task.
 */
@Entity
@Table(name = "time_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "task_id")
    private UUID taskId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    private Task task;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * Duration in minutes, calculated when the timer is stopped
     */
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Indicates whether this entry was manually created (true) or from the timer (false)
     */
    @Column(name = "is_manual", nullable = false)
    @Builder.Default
    private Boolean isManual = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Check if this time entry is currently running (timer is active)
     */
    @Transient
    public boolean isRunning() {
        return endTime == null;
    }

    /**
     * Get duration in hours (for display purposes)
     */
    @Transient
    public Double getDurationHours() {
        if (durationMinutes == null) {
            return 0.0;
        }
        return durationMinutes / 60.0;
    }
}
