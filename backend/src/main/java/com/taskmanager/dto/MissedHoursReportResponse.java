package com.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissedHoursReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalMissedHours;
    private int daysWithMissedHours;
    private List<DailyMissedHours> dailyMissedHours;
    private WeeklySummary weeklySummary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyMissedHours {
        private LocalDate date;
        private String dayOfWeek;
        private BigDecimal expectedHours;
        private BigDecimal actualHours;
        private BigDecimal missedHours; // Positive if missed hours, 0 if met or exceeded
        private boolean isWorkingDay;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WeeklySummary {
        private BigDecimal expectedHoursPerWeek;
        private BigDecimal actualHoursThisWeek;
        private BigDecimal missedHours;
    }
}
