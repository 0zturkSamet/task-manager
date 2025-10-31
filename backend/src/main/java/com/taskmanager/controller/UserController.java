package com.taskmanager.controller;

import com.taskmanager.dto.UpdateUserRequest;
import com.taskmanager.dto.UserResponse;
import com.taskmanager.dto.UserStatisticsResponse;
import com.taskmanager.entity.User;
import com.taskmanager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "Endpoints for user profile management")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Returns the current user's profile information")
    public ResponseEntity<UserResponse> getUserProfile(@AuthenticationPrincipal User user) {
        UserResponse response = userService.getUserProfile(user.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Updates the current user's profile information")
    public ResponseEntity<UserResponse> updateUserProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        UserResponse response = userService.updateUserProfile(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/account")
    @Operation(summary = "Delete user account", description = "Soft deletes the current user's account")
    public ResponseEntity<?> deleteUserAccount(@AuthenticationPrincipal User user) {
        userService.deleteUserAccount(user.getId());
        return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all users", description = "Returns all active users in the system for task assignment")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search for users by email, first name, or last name")
    public ResponseEntity<List<UserResponse>> searchUsers(
            @Parameter(description = "Search term") @RequestParam String q
    ) {
        List<UserResponse> users = userService.searchUsers(q);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get user statistics", description = "Returns comprehensive statistics for the current user across all their projects and tasks")
    public ResponseEntity<UserStatisticsResponse> getUserStatistics(@AuthenticationPrincipal User user) {
        UserStatisticsResponse statistics = userService.getUserStatistics(user.getId());
        return ResponseEntity.ok(statistics);
    }
}
