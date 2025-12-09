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
public class OvertimeReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalOvertimeHours;
    private List<DailyOvertime> dailyOvertimes;
    private WeeklySummary weeklySummary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyOvertime {
        private LocalDate date;
        private String dayOfWeek;
        private BigDecimal expectedHours;
        private BigDecimal actualHours;
        private BigDecimal overtimeHours; // Positive if overtime, 0 if no overtime
        private boolean isWorkingDay;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WeeklySummary {
        private BigDecimal expectedHoursPerWeek;
        private BigDecimal actualHoursThisWeek;
        private BigDecimal overtimeHours;
    }
}
