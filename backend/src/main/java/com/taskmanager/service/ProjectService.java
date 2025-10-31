package com.taskmanager.service;

import com.taskmanager.dto.*;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.ProjectMember;
import com.taskmanager.entity.ProjectRole;
import com.taskmanager.entity.User;
import com.taskmanager.exception.DuplicateResourceException;
import com.taskmanager.exception.ForbiddenException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.repository.ProjectMemberRepository;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProjectResponse createProject(UUID userId, CreateProjectRequest request) {
        log.info("Creating new project for user ID: {}", userId);

        // Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Create project
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .color(request.getColor() != null ? request.getColor() : "#3B82F6")
                .ownerId(userId)
                .isActive(true)
                .build();

        Project savedProject = projectRepository.save(project);

        // Automatically add owner as a member with OWNER role
        ProjectMember ownerMember = ProjectMember.builder()
                .projectId(savedProject.getId())
                .userId(userId)
                .role(ProjectRole.OWNER)
                .build();
        projectMemberRepository.save(ownerMember);

        log.info("Project created successfully with ID: {}", savedProject.getId());
        return mapToProjectResponse(savedProject, user);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllUserProjects(UUID userId) {
        log.info("Fetching all projects for user ID: {}", userId);

        // Get the user to check if they're an admin
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Project> projects;

        // If user is admin, return ALL projects in the system
        if (user.isAdmin()) {
            log.info("User is admin - fetching ALL projects");
            projects = projectRepository.findAllByIsActiveTrueOrderByCreatedAtDesc();
        } else {
            // Regular user - only show projects they're part of
            projects = projectRepository.findAllUserProjects(userId);
        }

        return projects.stream()
                .map(project -> {
                    User owner = userRepository.findById(project.getOwnerId()).orElse(null);
                    return mapToProjectResponse(project, owner);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID userId, UUID projectId) {
        log.info("Fetching project ID: {} for user ID: {}", projectId, userId);

        Project project = projectRepository.findByIdAndIsActiveTrue(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Get the user to check if they're an admin
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Admins can access any project, regular users need membership
        if (!user.isAdmin() && !hasAccess(userId, projectId)) {
            throw new ForbiddenException("You don't have access to this project");
        }

        User owner = userRepository.findById(project.getOwnerId()).orElse(null);
        return mapToProjectResponse(project, owner);
    }

    @Transactional
    public ProjectResponse updateProject(UUID userId, UUID projectId, UpdateProjectRequest request) {
        log.info("Updating project ID: {} by user ID: {}", projectId, userId);

        Project project = projectRepository.findByIdAndIsActiveTrue(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Check if user has permission to edit (OWNER or ADMIN)
        if (!canEdit(userId, projectId)) {
            throw new ForbiddenException("You don't have permission to edit this project");
        }

        // Update fields if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getColor() != null) {
            project.setColor(request.getColor());
        }

        Project updatedProject = projectRepository.save(project);
        log.info("Project updated successfully: {}", projectId);

        User owner = userRepository.findById(updatedProject.getOwnerId()).orElse(null);
        return mapToProjectResponse(updatedProject, owner);
    }

    @Transactional
    public void deleteProject(UUID userId, UUID projectId) {
        log.info("Deleting project ID: {} by user ID: {}", projectId, userId);

        Project project = projectRepository.findByIdAndIsActiveTrue(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Only owner can delete project
        if (!isOwner(userId, projectId)) {
            throw new ForbiddenException("Only the project owner can delete this project");
        }

        // Soft delete
        project.setIsActive(false);
        projectRepository.save(project);

        log.info("Project soft deleted successfully: {}", projectId);
    }

    @Transactional
    public ProjectMemberResponse addMember(UUID userId, UUID projectId, AddMemberRequest request) {
        log.info("Adding member to project ID: {} by user ID: {}", projectId, userId);

        // Verify project exists
        Project project = projectRepository.findByIdAndIsActiveTrue(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Only owner can add members
        if (!isOwner(userId, projectId)) {
            throw new ForbiddenException("Only the project owner can add members");
        }

        // Find user to add (by userId or email)
        User userToAdd;
        if (request.getUserId() != null) {
            userToAdd = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        } else if (request.getEmail() != null) {
            userToAdd = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User with email " + request.getEmail() + " not found"));
        } else {
            throw new IllegalArgumentException("Either userId or email must be provided");
        }

        // Check if user is already a member
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, userToAdd.getId())) {
            throw new DuplicateResourceException("User is already a member of this project");
        }

        // Add member
        ProjectMember member = ProjectMember.builder()
                .projectId(projectId)
                .userId(userToAdd.getId())
                .role(request.getRole())
                .build();

        ProjectMember savedMember = projectMemberRepository.save(member);
        log.info("Member added successfully to project: {}", projectId);

        return mapToProjectMemberResponse(savedMember, userToAdd);
    }

    @Transactional
    public ProjectMemberResponse updateMemberRole(UUID userId, UUID projectId, UUID memberId, ProjectRole newRole) {
        log.info("Updating role for member ID: {} in project ID: {}", memberId, projectId);

        // Verify project exists
        projectRepository.findByIdAndIsActiveTrue(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Only owner can update member roles
        if (!isOwner(userId, projectId)) {
            throw new ForbiddenException("Only the project owner can update member roles");
        }

        // Find member
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in this project"));

        // Prevent owner from changing their own role
        if (member.getUserId().equals(userId)) {
            throw new ForbiddenException("You cannot change your own role");
        }

        // Update role
        member.setRole(newRole);
        ProjectMember updatedMember = projectMemberRepository.save(member);

        User memberUser = userRepository.findById(memberId).orElse(null);
        log.info("Member role updated successfully");

        return mapToProjectMemberResponse(updatedMember, memberUser);
    }

    @Transactional
    public void removeMember(UUID userId, UUID projectId, UUID memberId) {
        log.info("Removing member ID: {} from project ID: {}", memberId, projectId);

        // Verify project exists
        projectRepository.findByIdAndIsActiveTrue(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Only owner can remove members
        if (!isOwner(userId, projectId)) {
            throw new ForbiddenException("Only the project owner can remove members");
        }

        // Prevent owner from removing themselves
        if (memberId.equals(userId)) {
            throw new ForbiddenException("Project owner cannot be removed. Delete the project instead.");
        }

        // Remove member
        projectMemberRepository.deleteByProjectIdAndUserId(projectId, memberId);
        log.info("Member removed successfully from project: {}", projectId);
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getProjectMembers(UUID userId, UUID projectId) {
        log.info("Fetching members for project ID: {}", projectId);

        // Verify project exists
        projectRepository.findByIdAndIsActiveTrue(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Get the user to check if they're an admin
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Admins can view members of any project, regular users need access
        if (!user.isAdmin() && !hasAccess(userId, projectId)) {
            throw new ForbiddenException("You don't have access to this project");
        }

        List<ProjectMember> members = projectMemberRepository.findByProjectIdOrderByJoinedAtAsc(projectId);

        return members.stream()
                .map(member -> {
                    User memberUser = userRepository.findById(member.getUserId()).orElse(null);
                    return mapToProjectMemberResponse(member, memberUser);
                })
                .collect(Collectors.toList());
    }

    // Permission helper methods
    private boolean hasAccess(UUID userId, UUID projectId) {
        return projectRepository.hasAccess(projectId, userId);
    }

    private boolean isOwner(UUID userId, UUID projectId) {
        return projectRepository.isOwner(projectId, userId);
    }

    private boolean canEdit(UUID userId, UUID projectId) {
        if (isOwner(userId, projectId)) {
            return true;
        }

        ProjectRole role = projectMemberRepository.findRoleByProjectIdAndUserId(projectId, userId)
                .orElse(null);

        return role == ProjectRole.ADMIN || role == ProjectRole.OWNER;
    }

    // Mapping methods
    private ProjectResponse mapToProjectResponse(Project project, User owner) {
        List<ProjectMember> members = projectMemberRepository.findByProjectIdOrderByJoinedAtAsc(project.getId());

        List<ProjectMemberResponse> memberResponses = members.stream()
                .map(member -> {
                    User memberUser = userRepository.findById(member.getUserId()).orElse(null);
                    return mapToProjectMemberResponse(member, memberUser);
                })
                .collect(Collectors.toList());

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .color(project.getColor())
                .ownerId(project.getOwnerId())
                .ownerName(owner != null ? owner.getFirstName() + " " + owner.getLastName() : "Unknown")
                .isActive(project.getIsActive())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .members(memberResponses)
                .memberCount(memberResponses.size())
                .build();
    }

    private ProjectMemberResponse mapToProjectMemberResponse(ProjectMember member, User user) {
        return ProjectMemberResponse.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .userEmail(user != null ? user.getEmail() : "Unknown")
                .userName(user != null ? user.getFirstName() + " " + user.getLastName() : "Unknown")
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
