package com.taskmanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.dto.WorkingHoursConfigDTO;
import com.taskmanager.entity.User;
import com.taskmanager.entity.UserWorkingHours;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.repository.UserWorkingHoursRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkingHoursService {

    private final UserWorkingHoursRepository workingHoursRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public WorkingHoursConfigDTO getOrCreateConfig(String userId) {
        UserWorkingHours config = workingHoursRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultConfig(userId));
        return toDTO(config);
    }

    @Transactional
    public WorkingHoursConfigDTO updateConfig(String userId, WorkingHoursConfigDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserWorkingHours config = workingHoursRepository.findByUserId(userId)
                .orElseGet(() -> UserWorkingHours.builder()
                        .user(user)
                        .build());

        if (dto.getExpectedHoursPerDay() != null) {
            validateHours(dto.getExpectedHoursPerDay(), "Expected hours per day");
            config.setExpectedHoursPerDay(dto.getExpectedHoursPerDay());
        }

        if (dto.getExpectedHoursPerWeek() != null) {
            validateHours(dto.getExpectedHoursPerWeek(), "Expected hours per week");
            config.setExpectedHoursPerWeek(dto.getExpectedHoursPerWeek());
        }

        if (dto.getWorkingDays() != null && !dto.getWorkingDays().isEmpty()) {
            validateWorkingDays(dto.getWorkingDays());
            try {
                config.setWorkingDays(objectMapper.writeValueAsString(dto.getWorkingDays()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize working days", e);
            }
        }

        UserWorkingHours saved = workingHoursRepository.save(config);
        return toDTO(saved);
    }

    private UserWorkingHours createDefaultConfig(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserWorkingHours defaultConfig = UserWorkingHours.getDefault(user);
        return workingHoursRepository.save(defaultConfig);
    }

    private WorkingHoursConfigDTO toDTO(UserWorkingHours config) {
        List<String> workingDays;
        try {
            workingDays = objectMapper.readValue(
                    config.getWorkingDays(),
                    new TypeReference<List<String>>() {}
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize working days", e);
        }

        return WorkingHoursConfigDTO.builder()
                .id(config.getId())
                .expectedHoursPerDay(config.getExpectedHoursPerDay())
                .expectedHoursPerWeek(config.getExpectedHoursPerWeek())
                .workingDays(workingDays)
                .build();
    }

    private void validateHours(BigDecimal hours, String fieldName) {
        if (hours.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        if (hours.compareTo(new BigDecimal("24")) > 0) {
            throw new IllegalArgumentException(fieldName + " cannot exceed 24 hours");
        }
    }

    private void validateWorkingDays(List<String> workingDays) {
        List<String> validDays = List.of(
                "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
        );

        for (String day : workingDays) {
            if (!validDays.contains(day.toUpperCase())) {
                throw new IllegalArgumentException("Invalid working day: " + day);
            }
        }

        if (workingDays.isEmpty()) {
            throw new IllegalArgumentException("At least one working day must be specified");
        }
    }
}
