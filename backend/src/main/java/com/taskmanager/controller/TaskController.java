package com.taskmanager.controller;

import com.taskmanager.dto.*;
import com.taskmanager.entity.User;
import com.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Endpoints for task management")
@SecurityRequirement(name = "Bearer Authentication")
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/tasks")
    @Operation(summary = "Create a new task", description = "Creates a new task in a project. Requires OWNER or EDITOR role in the project.")
    public ResponseEntity<TaskResponse> createTask(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        TaskResponse response = taskService.createTask(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/tasks")
    @Operation(summary = "Get all user tasks", description = "Returns all tasks across all projects the user has access to")
    public ResponseEntity<List<TaskResponse>> getAllUserTasks(
            @AuthenticationPrincipal User user
    ) {
        List<TaskResponse> tasks = taskService.getAllUserTasks(user.getId());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/projects/{projectId}/tasks")
    @Operation(summary = "Get project tasks", description = "Returns all tasks for a specific project")
    public ResponseEntity<List<TaskResponse>> getProjectTasks(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Project ID") @PathVariable UUID projectId
    ) {
        List<TaskResponse> tasks = taskService.getProjectTasks(user.getId(), projectId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "Get task by ID", description = "Returns detailed information about a specific task")
    public ResponseEntity<TaskResponse> getTaskById(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Task ID") @PathVariable UUID taskId
    ) {
        TaskResponse response = taskService.getTaskById(user.getId(), taskId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/tasks/{taskId}")
    @Operation(summary = "Update task", description = "Updates task details. Requires OWNER or EDITOR role in the project.")
    public ResponseEntity<TaskResponse> updateTask(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Task ID") @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        TaskResponse response = taskService.updateTask(user.getId(), taskId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/tasks/{taskId}")
    @Operation(summary = "Delete task", description = "Soft deletes a task. Only project owner or task creator can delete.")
    public ResponseEntity<?> deleteTask(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Task ID") @PathVariable UUID taskId
    ) {
        taskService.deleteTask(user.getId(), taskId);
        return ResponseEntity.ok(Map.of("message", "Task deleted successfully"));
    }

    @PostMapping("/tasks/filter")
    @Operation(summary = "Filter tasks", description = "Returns tasks filtered by multiple criteria (status, priority, assignee, due date, etc.)")
    public ResponseEntity<List<TaskResponse>> filterTasks(
            @AuthenticationPrincipal User user,
            @RequestBody TaskFilterRequest filter
    ) {
        List<TaskResponse> tasks = taskService.filterTasks(user.getId(), filter);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/projects/{projectId}/tasks/statistics")
    @Operation(summary = "Get task statistics", description = "Returns comprehensive statistics about tasks in a project (counts by status, priority, completion rates, etc.)")
    public ResponseEntity<TaskStatisticsResponse> getProjectStatistics(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Project ID") @PathVariable UUID projectId
    ) {
        TaskStatisticsResponse statistics = taskService.getProjectStatistics(user.getId(), projectId);
        return ResponseEntity.ok(statistics);
    }

    @PostMapping("/tasks/{taskId}/comments")
    @Operation(summary = "Add comment to task", description = "Adds a comment/note to a task for collaboration and activity tracking")
    public ResponseEntity<TaskCommentResponse> addComment(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Task ID") @PathVariable UUID taskId,
            @Valid @RequestBody CreateTaskCommentRequest request
    ) {
        TaskCommentResponse response = taskService.addComment(user.getId(), taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/tasks/{taskId}/comments")
    @Operation(summary = "Get task comments", description = "Returns all comments for a specific task")
    public ResponseEntity<List<TaskCommentResponse>> getTaskComments(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Task ID") @PathVariable UUID taskId
    ) {
        List<TaskCommentResponse> comments = taskService.getTaskComments(user.getId(), taskId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/comments/{commentId}/like")
    @Operation(summary = "Like a comment", description = "Increments the like count for a comment")
    public ResponseEntity<TaskCommentResponse> likeComment(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Comment ID") @PathVariable UUID commentId
    ) {
        TaskCommentResponse response = taskService.likeComment(user.getId(), commentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/comments/{commentId}/dislike")
    @Operation(summary = "Dislike a comment", description = "Increments the dislike count for a comment")
    public ResponseEntity<TaskCommentResponse> dislikeComment(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Comment ID") @PathVariable UUID commentId
    ) {
        TaskCommentResponse response = taskService.dislikeComment(user.getId(), commentId);
        return ResponseEntity.ok(response);
    }
}
