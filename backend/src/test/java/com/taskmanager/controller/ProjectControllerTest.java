package com.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.dto.*;
import com.taskmanager.entity.ProjectRole;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ForbiddenException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.security.JwtAuthenticationFilter;
import com.taskmanager.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = ProjectController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
@DisplayName("ProjectController Integration Tests")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    private User authenticatedUser;
    private UUID userId;
    private UUID projectId;
    private ProjectResponse projectResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        authenticatedUser = User.builder()
                .id(userId)
                .email("owner@example.com")
                .password("encodedPassword")
                .firstName("Owner")
                .lastName("User")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        projectResponse = ProjectResponse.builder()
                .id(projectId)
                .name("Test Project")
                .description("Test Description")
                .color("#FF0000")
                .ownerId(userId)
                .members(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/projects - Should create project successfully")
    @WithMockUser
    void createProject_Success() throws Exception {
        // Arrange
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("New Project")
                .description("Project Description")
                .color("#FF0000")
                .build();

        when(projectService.createProject(eq(userId), any(CreateProjectRequest.class)))
                .thenReturn(projectResponse);

        // Act & Assert
        mockMvc.perform(post("/api/projects")
                .with(user(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Project"))
                .andExpect(jsonPath("$.ownerId").value(userId.toString()));

        verify(projectService).createProject(eq(userId), any(CreateProjectRequest.class));
    }

    @Test
    @DisplayName("POST /api/projects - Should return 400 when name is missing")
    @WithMockUser
    void createProject_MissingName_BadRequest() throws Exception {
        // Arrange
        CreateProjectRequest request = CreateProjectRequest.builder()
                .description("Project Description")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/projects")
                .with(user(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(projectService, never()).createProject(any(), any());
    }

    @Test
    @DisplayName("POST /api/projects - Should return 401 when not authenticated")
    void createProject_NotAuthenticated_Unauthorized() throws Exception {
        // Arrange
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("New Project")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(projectService, never()).createProject(any(), any());
    }

    @Test
    @DisplayName("GET /api/projects - Should get all user projects")
    @WithMockUser
    void getAllUserProjects_Success() throws Exception {
        // Arrange
        List<ProjectResponse> projects = Arrays.asList(projectResponse);
        when(projectService.getAllUserProjects(userId)).thenReturn(projects);

        // Act & Assert
        mockMvc.perform(get("/api/projects")
                .with(user(authenticatedUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Project"));

        verify(projectService).getAllUserProjects(userId);
    }

    @Test
    @DisplayName("GET /api/projects - Should return empty list when user has no projects")
    @WithMockUser
    void getAllUserProjects_EmptyList() throws Exception {
        // Arrange
        when(projectService.getAllUserProjects(userId)).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/projects")
                .with(user(authenticatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/projects/{projectId} - Should get project by ID")
    @WithMockUser
    void getProjectById_Success() throws Exception {
        // Arrange
        when(projectService.getProjectById(userId, projectId)).thenReturn(projectResponse);

        // Act & Assert
        mockMvc.perform(get("/api/projects/{projectId}", projectId)
                .with(user(authenticatedUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("Test Project"));

        verify(projectService).getProjectById(userId, projectId);
    }

    @Test
    @DisplayName("GET /api/projects/{projectId} - Should return 404 when project not found")
    @WithMockUser
    void getProjectById_NotFound() throws Exception {
        // Arrange
        when(projectService.getProjectById(userId, projectId))
                .thenThrow(new ResourceNotFoundException("Project not found"));

        // Act & Assert
        mockMvc.perform(get("/api/projects/{projectId}", projectId)
                .with(user(authenticatedUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/projects/{projectId} - Should return 403 when user has no access")
    @WithMockUser
    void getProjectById_NoAccess_Forbidden() throws Exception {
        // Arrange
        when(projectService.getProjectById(userId, projectId))
                .thenThrow(new ForbiddenException("You don't have access to this project"));

        // Act & Assert
        mockMvc.perform(get("/api/projects/{projectId}", projectId)
                .with(user(authenticatedUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/projects/{projectId} - Should update project")
    @WithMockUser
    void updateProject_Success() throws Exception {
        // Arrange
        UpdateProjectRequest request = UpdateProjectRequest.builder()
                .name("Updated Project")
                .description("Updated Description")
                .build();

        ProjectResponse updatedResponse = ProjectResponse.builder()
                .id(projectId)
                .name("Updated Project")
                .description("Updated Description")
                .color("#FF0000")
                .ownerId(userId)
                .members(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(projectService.updateProject(eq(userId), eq(projectId), any(UpdateProjectRequest.class)))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/projects/{projectId}", projectId)
                .with(user(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Project"))
                .andExpect(jsonPath("$.description").value("Updated Description"));

        verify(projectService).updateProject(eq(userId), eq(projectId), any(UpdateProjectRequest.class));
    }

    @Test
    @DisplayName("PUT /api/projects/{projectId} - Should return 403 when user lacks permission")
    @WithMockUser
    void updateProject_NoPermission_Forbidden() throws Exception {
        // Arrange
        UpdateProjectRequest request = UpdateProjectRequest.builder()
                .name("Updated Project")
                .build();

        when(projectService.updateProject(eq(userId), eq(projectId), any(UpdateProjectRequest.class)))
                .thenThrow(new ForbiddenException("You don't have permission to edit this project"));

        // Act & Assert
        mockMvc.perform(put("/api/projects/{projectId}", projectId)
                .with(user(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/projects/{projectId} - Should delete project")
    @WithMockUser
    void deleteProject_Success() throws Exception {
        // Arrange
        doNothing().when(projectService).deleteProject(userId, projectId);

        // Act & Assert
        mockMvc.perform(delete("/api/projects/{projectId}", projectId)
                .with(user(authenticatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Project deleted successfully"));

        verify(projectService).deleteProject(userId, projectId);
    }

    @Test
    @DisplayName("DELETE /api/projects/{projectId} - Should return 403 when user is not owner")
    @WithMockUser
    void deleteProject_NotOwner_Forbidden() throws Exception {
        // Arrange
        doThrow(new ForbiddenException("Only the project owner can delete this project"))
                .when(projectService).deleteProject(userId, projectId);

        // Act & Assert
        mockMvc.perform(delete("/api/projects/{projectId}", projectId)
                .with(user(authenticatedUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/projects/{projectId}/members - Should get project members")
    @WithMockUser
    void getProjectMembers_Success() throws Exception {
        // Arrange
        ProjectMemberResponse member = ProjectMemberResponse.builder()
                .userId(userId)
                .userEmail("owner@example.com")
                .userName("Owner User")
                .role(ProjectRole.OWNER)
                .joinedAt(LocalDateTime.now())
                .build();

        when(projectService.getProjectMembers(userId, projectId)).thenReturn(List.of(member));

        // Act & Assert
        mockMvc.perform(get("/api/projects/{projectId}/members", projectId)
                .with(user(authenticatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].role").value("OWNER"));

        verify(projectService).getProjectMembers(userId, projectId);
    }

    @Test
    @DisplayName("POST /api/projects/{projectId}/members - Should add member")
    @WithMockUser
    void addMember_Success() throws Exception {
        // Arrange
        UUID newMemberId = UUID.randomUUID();
        AddMemberRequest request = AddMemberRequest.builder()
                .email("member@example.com")
                .role(ProjectRole.MEMBER)
                .build();

        ProjectMemberResponse response = ProjectMemberResponse.builder()
                .userId(newMemberId)
                .userEmail("member@example.com")
                .userName("Member User")
                .role(ProjectRole.MEMBER)
                .joinedAt(LocalDateTime.now())
                .build();

        when(projectService.addMember(eq(userId), eq(projectId), any(AddMemberRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                .with(user(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("member@example.com"))
                .andExpect(jsonPath("$.role").value("MEMBER"));

        verify(projectService).addMember(eq(userId), eq(projectId), any(AddMemberRequest.class));
    }

    @Test
    @DisplayName("PUT /api/projects/{projectId}/members/{memberId}/role - Should update member role")
    @WithMockUser
    void updateMemberRole_Success() throws Exception {
        // Arrange
        UUID memberId = UUID.randomUUID();
        Map<String, ProjectRole> roleUpdate = Map.of("role", ProjectRole.ADMIN);

        ProjectMemberResponse response = ProjectMemberResponse.builder()
                .userId(memberId)
                .userEmail("member@example.com")
                .userName("Member User")
                .role(ProjectRole.ADMIN)
                .joinedAt(LocalDateTime.now())
                .build();

        when(projectService.updateMemberRole(userId, projectId, memberId, ProjectRole.ADMIN))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/projects/{projectId}/members/{memberId}/role", projectId, memberId)
                .with(user(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(projectService).updateMemberRole(userId, projectId, memberId, ProjectRole.ADMIN);
    }

    @Test
    @DisplayName("PUT /api/projects/{projectId}/members/{memberId}/role - Should return 400 when role is missing")
    @WithMockUser
    void updateMemberRole_MissingRole_BadRequest() throws Exception {
        // Arrange
        UUID memberId = UUID.randomUUID();
        Map<String, String> emptyRoleUpdate = new HashMap<>();

        // Act & Assert
        mockMvc.perform(put("/api/projects/{projectId}/members/{memberId}/role", projectId, memberId)
                .with(user(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRoleUpdate)))
                .andExpect(status().isBadRequest());

        verify(projectService, never()).updateMemberRole(any(), any(), any(), any());
    }

    @Test
    @DisplayName("DELETE /api/projects/{projectId}/members/{memberId} - Should remove member")
    @WithMockUser
    void removeMember_Success() throws Exception {
        // Arrange
        UUID memberId = UUID.randomUUID();
        doNothing().when(projectService).removeMember(userId, projectId, memberId);

        // Act & Assert
        mockMvc.perform(delete("/api/projects/{projectId}/members/{memberId}", projectId, memberId)
                .with(user(authenticatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Member removed successfully"));

        verify(projectService).removeMember(userId, projectId, memberId);
    }

    @Test
    @DisplayName("DELETE /api/projects/{projectId}/members/{memberId} - Should return 403 when not owner")
    @WithMockUser
    void removeMember_NotOwner_Forbidden() throws Exception {
        // Arrange
        UUID memberId = UUID.randomUUID();
        doThrow(new ForbiddenException("Only the project owner can remove members"))
                .when(projectService).removeMember(userId, projectId, memberId);

        // Act & Assert
        mockMvc.perform(delete("/api/projects/{projectId}/members/{memberId}", projectId, memberId)
                .with(user(authenticatedUser)))
                .andExpect(status().isForbidden());
    }
}
