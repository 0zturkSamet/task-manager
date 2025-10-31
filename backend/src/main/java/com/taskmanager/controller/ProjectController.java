package com.taskmanager.controller;

import com.taskmanager.dto.*;
import com.taskmanager.entity.ProjectRole;
import com.taskmanager.entity.User;
import com.taskmanager.service.ProjectService;
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
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Endpoints for project management")
@SecurityRequirement(name = "Bearer Authentication")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Create a new project", description = "Creates a new project with the current user as owner")
    public ResponseEntity<ProjectResponse> createProject(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateProjectRequest request
    ) {
        ProjectResponse response = projectService.createProject(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all user projects", description = "Returns all projects where the user is owner or member")
    public ResponseEntity<List<ProjectResponse>> getAllUserProjects(
            @AuthenticationPrincipal User user
    ) {
        List<ProjectResponse> projects = projectService.getAllUserProjects(user.getId());
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "Get project by ID", description = "Returns detailed information about a specific project")
    public ResponseEntity<ProjectResponse> getProjectById(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Project ID") @PathVariable UUID projectId
    ) {
        ProjectResponse response = projectService.getProjectById(user.getId(), projectId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{projectId}")
    @Operation(summary = "Update project", description = "Updates project details. Requires OWNER or ADMIN role.")
    public ResponseEntity<ProjectResponse> updateProject(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Project ID") @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        ProjectResponse response = projectService.updateProject(user.getId(), projectId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "Delete project", description = "Soft deletes a project. Only the OWNER can delete.")
    public ResponseEntity<?> deleteProject(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Project ID") @PathVariable UUID projectId
    ) {
        projectService.deleteProject(user.getId(), projectId);
        return ResponseEntity.ok(Map.of("message", "Project deleted successfully"));
    }

    @GetMapping("/{projectId}/members")
    @Operation(summary = "Get project members", description = "Returns all members of a project with their roles")
    public ResponseEntity<List<ProjectMemberResponse>> getProjectMembers(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Project ID") @PathVariable UUID projectId
    ) {
        List<ProjectMemberResponse> members = projectService.getProjectMembers(user.getId(), projectId);
        return ResponseEntity.ok(members);
    }

    @PostMapping("/{projectId}/members")
    @Operation(summary = "Add project member", description = "Adds a new member to the project. Only OWNER can add members.")
    public ResponseEntity<ProjectMemberResponse> addMember(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Project ID") @PathVariable UUID projectId,
            @Valid @RequestBody AddMemberRequest request
    ) {
        ProjectMemberResponse response = projectService.addMember(user.getId(), projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{projectId}/members/{memberId}/role")
    @Operation(summary = "Update member role", description = "Updates a member's role in the project. Only OWNER can update roles.")
    public ResponseEntity<ProjectMemberResponse> updateMemberRole(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Project ID") @PathVariable UUID projectId,
            @Parameter(description = "Member User ID") @PathVariable UUID memberId,
            @RequestBody Map<String, ProjectRole> roleUpdate
    ) {
        ProjectRole newRole = roleUpdate.get("role");
        if (newRole == null) {
            throw new IllegalArgumentException("Role is required");
        }
        ProjectMemberResponse response = projectService.updateMemberRole(user.getId(), projectId, memberId, newRole);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    @Operation(summary = "Remove project member", description = "Removes a member from the project. Only OWNER can remove members.")
    public ResponseEntity<?> removeMember(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Project ID") @PathVariable UUID projectId,
            @Parameter(description = "Member User ID") @PathVariable UUID memberId
    ) {
        projectService.removeMember(user.getId(), projectId, memberId);
        return ResponseEntity.ok(Map.of("message", "Member removed successfully"));
    }
}
