package com.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkingHoursConfigDTO {
    private String id;
    private BigDecimal expectedHoursPerDay;
    private BigDecimal expectedHoursPerWeek;
    private List<String> workingDays; // ["MONDAY", "TUESDAY", etc.]
}
