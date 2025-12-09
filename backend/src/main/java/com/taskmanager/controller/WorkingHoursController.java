package com.taskmanager.controller;

import com.taskmanager.dto.WorkingHoursConfigDTO;
import com.taskmanager.service.WorkingHoursService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/working-hours")
@RequiredArgsConstructor
public class WorkingHoursController {

    private final WorkingHoursService workingHoursService;

    @GetMapping("/config")
    public ResponseEntity<WorkingHoursConfigDTO> getConfig(Authentication authentication) {
        String userId = authentication.getName();
        WorkingHoursConfigDTO config = workingHoursService.getOrCreateConfig(userId);
        return ResponseEntity.ok(config);
    }

    @PostMapping("/config")
    public ResponseEntity<WorkingHoursConfigDTO> createConfig(
            Authentication authentication,
            @RequestBody WorkingHoursConfigDTO dto) {
        String userId = authentication.getName();
        WorkingHoursConfigDTO config = workingHoursService.updateConfig(userId, dto);
        return ResponseEntity.ok(config);
    }

    @PutMapping("/config")
    public ResponseEntity<WorkingHoursConfigDTO> updateConfig(
            Authentication authentication,
            @RequestBody WorkingHoursConfigDTO dto) {
        String userId = authentication.getName();
        WorkingHoursConfigDTO config = workingHoursService.updateConfig(userId, dto);
        return ResponseEntity.ok(config);
    }
}
