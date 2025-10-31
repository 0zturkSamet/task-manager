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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService Unit Tests")
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    private UUID userId;
    private UUID projectId;
    private UUID memberId;
    private User user;
    private User memberUser;
    private Project project;
    private ProjectMember projectMember;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        memberId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .email("owner@example.com")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .build();

        memberUser = User.builder()
                .id(memberId)
                .email("member@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .isActive(true)
                .build();

        project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .description("Test Description")
                .color("#3B82F6")
                .ownerId(userId)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        projectMember = ProjectMember.builder()
                .id(UUID.randomUUID())
                .projectId(projectId)
                .userId(userId)
                .role(ProjectRole.OWNER)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create project successfully and add owner as member")
    void createProject_Success() {
        // Arrange
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("New Project")
                .description("Project Description")
                .color("#FF5733")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(projectMember);
        when(projectMemberRepository.findByProjectIdOrderByJoinedAtAsc(projectId))
                .thenReturn(List.of(projectMember));

        // Act
        ProjectResponse response = projectService.createProject(userId, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(projectId);
        assertThat(response.getName()).isEqualTo(project.getName());

        // Verify owner was added as member
        ArgumentCaptor<ProjectMember> memberCaptor = ArgumentCaptor.forClass(ProjectMember.class);
        verify(projectMemberRepository).save(memberCaptor.capture());
        ProjectMember savedMember = memberCaptor.getValue();
        assertThat(savedMember.getUserId()).isEqualTo(userId);
        assertThat(savedMember.getRole()).isEqualTo(ProjectRole.OWNER);
    }

    @Test
    @DisplayName("Should use default color when not provided")
    void createProject_DefaultColor() {
        // Arrange
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("New Project")
                .description("Project Description")
                .build(); // No color provided

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(projectMember);
        when(projectMemberRepository.findByProjectIdOrderByJoinedAtAsc(any()))
                .thenReturn(List.of(projectMember));

        // Act
        projectService.createProject(userId, request);

        // Assert
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());
        Project savedProject = projectCaptor.getValue();
        assertThat(savedProject.getColor()).isEqualTo("#3B82F6"); // Default color
    }

    @Test
    @DisplayName("Should throw exception when user not found during project creation")
    void createProject_UserNotFound_ThrowsException() {
        // Arrange
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("New Project")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> projectService.createProject(userId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Should get all user projects")
    void getAllUserProjects_Success() {
        // Arrange
        when(projectRepository.findAllUserProjects(userId)).thenReturn(List.of(project));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(projectMemberRepository.findByProjectIdOrderByJoinedAtAsc(projectId))
                .thenReturn(List.of(projectMember));

        // Act
        List<ProjectResponse> projects = projectService.getAllUserProjects(userId);

        // Assert
        assertThat(projects).hasSize(1);
        assertThat(projects.get(0).getId()).isEqualTo(projectId);
        assertThat(projects.get(0).getOwnerId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should get project by ID when user has access")
    void getProjectById_Success() {
        // Arrange
        when(projectRepository.findByIdAndIsActiveTrue(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.hasAccess(projectId, userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(projectMemberRepository.findByProjectIdOrderByJoinedAtAsc(projectId))
                .thenReturn(List.of(projectMember));

        // Act
        ProjectResponse response = projectService.getProjectById(userId, projectId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(projectId);
    }

    @Test
    @DisplayName("Should throw exception when project not found")
    void getProjectById_ProjectNotFound_ThrowsException() {
        // Arrange
        when(projectRepository.findByIdAndIsActiveTrue(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> projectService.getProjectById(userId, projectId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project not found");
    }

    @Test
    @DisplayName("Should throw exception when user has no access to project")
    void getProjectById_NoAccess_ThrowsException() {
        // Arrange
        when(projectRepository.findByIdAndIsActiveTrue(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.hasAccess(projectId, userId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> projectService.getProjectById(userId, projectId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You don't have access to this project");
    }

    @Test
    @DisplayName("Should update project when user is owner")
    void updateProject_AsOwner_Success() {
        // Arrange
        UpdateProjectRequest request = UpdateProjectRequest.builder()
                .name("Updated Project")
                .description("Updated Description")
                .color("#00FF00")
                .build();

        when(projectRepository.findByIdAndIsActiveTrue(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.isOwner(projectId, userId)).thenReturn(true);
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(projectMemberRepository.findByProjectIdOrderByJoinedAtAsc(projectId))
                .thenReturn(List.of(projectMember));

        // Act
        ProjectResponse response = projectService.updateProject(userId, projectId, request);

        // Assert
        assertThat(response).isNotNull();
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("Should update project when user is admin")
    void updateProject_AsAdmin_Success() {
        // Arrange
        UpdateProjectRequest request = UpdateProjectRequest.builder()
                .name("Updated Project")
                .build();

        when(projectRepository.findByIdAndIsActiveTrue(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.isOwner(projectId, userId)).thenReturn(false);
        when(projectMemberRepository.findRoleByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.of(ProjectRole.ADMIN));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(projectMemberRepository.findByProjectIdOrderByJoinedAtAsc(projectId))
                .thenReturn(List.of(projectMember));

        // Act
        ProjectResponse response = projectService.updateProject(userId, projectId, request);

        // Assert
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when member tries to update project")
    void updateProject_AsMember_ThrowsException() {
        // Arrange
        UpdateProjectRequest request = UpdateProjectRequest.builder()
                .name("Updated Project")
                .build();

        when(projectRepository.findByIdAndIsActiveTrue(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.isOwner(projectId, userId)).thenReturn(false);
        when(projectMemberRepository.findRoleByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.of(ProjectRole.MEMBER));

        // Act & Assert
        assertThatThrownBy(() -> projectService.updateProject(userId, projectId, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You don't have permission to edit this project");
    }

    @Test
    @DisplayName("Should delete project when user is owner")
    void deleteProject_AsOwner_Success() {
        // Arrange
        when(projectRepository.findByIdAndIsActiveTrue(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.isOwner(projectId, userId)).thenReturn(true);
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        // Act
        projectService.deleteProject(userId, projectId);

        // Assert
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());
        assertThat(projectCaptor.getValue().getIsActive()).isFalse(); // Soft delete
    }

    @Test
    @DisplayName("Should throw exception when non-owner tries to delete project")
    void deleteProject_NonOwner_ThrowsException() {
        // Arrange
        when(projectRepository.findByIdAndIsActiveTrue(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.isOwner(projectId, userId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> projectService.deleteProject(userId, projectId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Only the project owner can delete this project");

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Should add member to project by userId")
    void addMember_ByUserId_Success() {
        // Arrange
        AddMemberRequest request = AddMemberRequest.builder()
                .userId(memberId)
                .role(ProjectRole.MEMBER)
                .build();

        when(projectRepository.findByIdAndIsActiveTrue(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.isOwner(projectId, userId)).thenReturn(true);
        when(userRepository.findById(memberId)).thenReturn(Optional.of(memberUser));
        when(projectMemberRepository.existsByProjectIdAndUserId(projectId, memberId)).thenReturn(false);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(projectMember);

        // Act
        ProjectMemberResponse response = projectService.addMember(userId, projectId, request);

        // Assert
        assertThat(response).isNotNull();
        verify(projectMemberRepository).save(any(ProjectMember.class));
    }

    @Test
    @DisplayName("Should add member to project by email")
    void addMember_ByEmail_Success() {
        // Arrange
        AddMemberRequest request = AddMemberRequest.builder()
                .email("member@example.com")
                .role(ProjectRole.MEMBER)
                .build();

        when(projectRepository.findByIdAndIsActiveTrue(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.isOwner(projectId, userId)).thenReturn(true);
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(memberUser));
        when(projectMemberRepository.existsByProjectIdAndUserId(projectId, memberId)).thenReturn(false);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(projectMember);

        // Act
        ProjectMemberResponse response = projectService.addMember(userId, projectId, request);

        // Assert
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when non-owner tries to add member")
    void addMember_NonOwner_ThrowsException() {
        // Arrange
        AddMemberRequest request = AddMemberRequest.builder()
                .userId(memberId)
                .role(ProjectRole.MEMBER)
                .build();

        when(projectRepository.findByIdAndIsActiveTrue(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.isOwner(projectId, userId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> projectService.addMember(userId, projectId, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Only the project owner can add members");
    }

    @Test
    @DisplayName("Should throw exception when adding duplicate member")
    void addMember_DuplicateMember_ThrowsException() {
        // Arrange
        AddMemberRequest request = AddMemberRequest.builder()
                .userId(memberId)
                .role(ProjectRole.MEMBER)
                .build();

        when(projectRepository.findByIdAndIsActiveTrue(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.isOwner(projectId, userId)).thenReturn(true);
        when(userRepository.findById(memberId)).thenReturn(Optional.of(memberUser));
        when(projectMemberRepository.existsByProjectIdAndUserId(projectId, memberId)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> projectService.addMember(userId, projectId, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("User is already a member of this project");
    }

    @Test
    @DisplayName("Should update member role")
    void updateMemberRole_Success() {
        // Arrange
        ProjectMember existingMember = ProjectMember.builder()
                .id(UUID.randomUUID())
                .projectId(projectId)
                .userId(memberId)
                .role(ProjectRole.MEMBER)
                .build();

        when(projectRepository.findByIdAndIsActiveTrue(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.isOwner(projectId, userId)).thenReturn(true);
        when(projectMemberRepository.findByProjectIdAndUserId(projectId, memberId))
                .thenReturn(Optional.of(existingMember));
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(existingMember);
        when(userRepository.findById(memberId)).thenReturn(Optional.of(memberUser));

        // Act
        ProjectMemberResponse response = projectService.updateMemberRole(userId, projectId, memberId, ProjectRole.ADMIN);

        // Assert
        assertThat(response).isNotNull();
        verify(projectMemberRepository).save(any(ProjectMember.class));
    }

    @Test
    @DisplayName("Should throw exception when owner tries to change own role")
    void updateMemberRole_OwnerChangingOwnRole_ThrowsException() {
        // Arrange
        ProjectMember ownerMember = ProjectMember.builder()
                .id(UUID.randomUUID())
                .projectId(projectId)
                .userId(userId)
                .role(ProjectRole.OWNER)
                .build();

        when(projectRepository.findByIdAndIsActiveTrue(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.isOwner(projectId, userId)).thenReturn(true);
        when(projectMemberRepository.findByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.of(ownerMember));

        // Act & Assert
        assertThatThrownBy(() -> projectService.updateMemberRole(userId, projectId, userId, ProjectRole.MEMBER))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You cannot change your own role");
    }

    @Test
    @DisplayName("Should remove member from project")
    void removeMember_Success() {
        // Arrange
        when(projectRepository.findByIdAndIsActiveTrue(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.isOwner(projectId, userId)).thenReturn(true);

        // Act
        projectService.removeMember(userId, projectId, memberId);

        // Assert
        verify(projectMemberRepository).deleteByProjectIdAndUserId(projectId, memberId);
    }

    @Test
    @DisplayName("Should throw exception when owner tries to remove themselves")
    void removeMember_OwnerRemovingThemselves_ThrowsException() {
        // Arrange
        when(projectRepository.findByIdAndIsActiveTrue(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.isOwner(projectId, userId)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> projectService.removeMember(userId, projectId, userId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Project owner cannot be removed. Delete the project instead.");
    }

    @Test
    @DisplayName("Should get project members")
    void getProjectMembers_Success() {
        // Arrange
        when(projectRepository.findByIdAndIsActiveTrue(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.hasAccess(projectId, userId)).thenReturn(true);
        when(projectMemberRepository.findByProjectIdOrderByJoinedAtAsc(projectId))
                .thenReturn(List.of(projectMember));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        List<ProjectMemberResponse> members = projectService.getProjectMembers(userId, projectId);

        // Assert
        assertThat(members).hasSize(1);
        assertThat(members.get(0).getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should throw exception when user without access tries to get members")
    void getProjectMembers_NoAccess_ThrowsException() {
        // Arrange
        when(projectRepository.findByIdAndIsActiveTrue(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.hasAccess(projectId, userId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> projectService.getProjectMembers(userId, projectId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You don't have access to this project");
    }
}
