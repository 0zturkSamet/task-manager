package com.taskmanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_working_hours")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWorkingHours {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "expected_hours_per_day", precision = 5, scale = 2, nullable = false)
    private BigDecimal expectedHoursPerDay;

    @Column(name = "expected_hours_per_week", precision = 5, scale = 2, nullable = false)
    private BigDecimal expectedHoursPerWeek;

    @Column(name = "working_days", columnDefinition = "TEXT", nullable = false)
    private String workingDays; // JSON array stored as string: ["MONDAY", "TUESDAY", ...]

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static UserWorkingHours getDefault(User user) {
        return UserWorkingHours.builder()
                .user(user)
                .expectedHoursPerDay(new BigDecimal("8.00"))
                .expectedHoursPerWeek(new BigDecimal("40.00"))
                .workingDays("[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\"]")
                .build();
    }
}
