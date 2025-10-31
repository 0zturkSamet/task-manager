package com.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.dto.*;
import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ForbiddenException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.security.JwtAuthenticationFilter;
import com.taskmanager.service.TaskService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = TaskController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
@DisplayName("TaskController Integration Tests")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    private User authenticatedUser;
    private UUID userId;
    private UUID projectId;
    private UUID taskId;
    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        authenticatedUser = User.builder()
                .id(userId)
                .email("user@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        taskResponse = TaskResponse.builder()
                .id(taskId)
                .title("Test Task")
                .description("Test Description")
                .projectId(projectId)
                .projectName("Test Project")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .position(0)
                .createdByUserId(userId)
                .createdByUserName("Test User")
                .commentCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/tasks - Should create task successfully")
    @WithMockUser
    void createTask_Success() throws Exception {
        // Arrange
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("New Task")
                .description("Task Description")
                .projectId(projectId)
                .priority(TaskPriority.HIGH)
                .build();

        when(taskService.createTask(eq(userId), any(CreateTaskRequest.class)))
                .thenReturn(taskResponse);

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                .with(user(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Test Task"));

        verify(taskService).createTask(eq(userId), any(CreateTaskRequest.class));
    }

    @Test
    @DisplayName("POST /api/tasks - Should return 400 when title is missing")
    @WithMockUser
    void createTask_MissingTitle_BadRequest() throws Exception {
        // Arrange
        CreateTaskRequest request = CreateTaskRequest.builder()
                .projectId(projectId)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                .with(user(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(any(), any());
    }

    @Test
    @DisplayName("POST /api/tasks - Should return 403 when user lacks permission")
    @WithMockUser
    void createTask_NoPermission_Forbidden() throws Exception {
        // Arrange
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("New Task")
                .projectId(projectId)
                .build();

        when(taskService.createTask(eq(userId), any(CreateTaskRequest.class)))
                .thenThrow(new ForbiddenException("You don't have permission to create tasks in this project"));

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                .with(user(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/tasks - Should get all user tasks")
    @WithMockUser
    void getAllUserTasks_Success() throws Exception {
        // Arrange
        when(taskService.getAllUserTasks(userId)).thenReturn(List.of(taskResponse));

        // Act & Assert
        mockMvc.perform(get("/api/tasks")
                .with(user(authenticatedUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Task"));

        verify(taskService).getAllUserTasks(userId);
    }

    @Test
    @DisplayName("GET /api/projects/{projectId}/tasks - Should get project tasks")
    @WithMockUser
    void getProjectTasks_Success() throws Exception {
        // Arrange
        when(taskService.getProjectTasks(userId, projectId)).thenReturn(List.of(taskResponse));

        // Act & Assert
        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                .with(user(authenticatedUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1));

        verify(taskService).getProjectTasks(userId, projectId);
    }

    @Test
    @DisplayName("GET /api/projects/{projectId}/tasks - Should return 403 when user has no access")
    @WithMockUser
    void getProjectTasks_NoAccess_Forbidden() throws Exception {
        // Arrange
        when(taskService.getProjectTasks(userId, projectId))
                .thenThrow(new ForbiddenException("You don't have access to this project"));

        // Act & Assert
        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                .with(user(authenticatedUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/tasks/{taskId} - Should get task by ID")
    @WithMockUser
    void getTaskById_Success() throws Exception {
        // Arrange
        when(taskService.getTaskById(userId, taskId)).thenReturn(taskResponse);

        // Act & Assert
        mockMvc.perform(get("/api/tasks/{taskId}", taskId)
                .with(user(authenticatedUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Test Task"));

        verify(taskService).getTaskById(userId, taskId);
    }

    @Test
    @DisplayName("GET /api/tasks/{taskId} - Should return 404 when task not found")
    @WithMockUser
    void getTaskById_NotFound() throws Exception {
        // Arrange
        when(taskService.getTaskById(userId, taskId))
                .thenThrow(new ResourceNotFoundException("Task not found"));

        // Act & Assert
        mockMvc.perform(get("/api/tasks/{taskId}", taskId)
                .with(user(authenticatedUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/tasks/{taskId} - Should update task successfully")
    @WithMockUser
    void updateTask_Success() throws Exception {
        // Arrange
        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .title("Updated Task")
                .status(TaskStatus.IN_PROGRESS)
                .build();

        TaskResponse updatedResponse = TaskResponse.builder()
                .id(taskId)
                .title("Updated Task")
                .status(TaskStatus.IN_PROGRESS)
                .projectId(projectId)
                .projectName("Test Project")
                .priority(TaskPriority.MEDIUM)
                .position(0)
                .createdByUserId(userId)
                .createdByUserName("Test User")
                .commentCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(taskService.updateTask(eq(userId), eq(taskId), any(UpdateTaskRequest.class)))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/tasks/{taskId}", taskId)
                .with(user(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        verify(taskService).updateTask(eq(userId), eq(taskId), any(UpdateTaskRequest.class));
    }

    @Test
    @DisplayName("PUT /api/tasks/{taskId} - Should return 403 when user lacks permission")
    @WithMockUser
    void updateTask_NoPermission_Forbidden() throws Exception {
        // Arrange
        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .title("Updated Task")
                .build();

        when(taskService.updateTask(eq(userId), eq(taskId), any(UpdateTaskRequest.class)))
                .thenThrow(new ForbiddenException("You don't have permission to edit tasks in this project"));

        // Act & Assert
        mockMvc.perform(put("/api/tasks/{taskId}", taskId)
                .with(user(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/tasks/{taskId} - Should delete task successfully")
    @WithMockUser
    void deleteTask_Success() throws Exception {
        // Arrange
        doNothing().when(taskService).deleteTask(userId, taskId);

        // Act & Assert
        mockMvc.perform(delete("/api/tasks/{taskId}", taskId)
                .with(user(authenticatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Task deleted successfully"));

        verify(taskService).deleteTask(userId, taskId);
    }

    @Test
    @DisplayName("DELETE /api/tasks/{taskId} - Should return 403 when user lacks permission")
    @WithMockUser
    void deleteTask_NoPermission_Forbidden() throws Exception {
        // Arrange
        doThrow(new ForbiddenException("Only project owner or task creator can delete tasks"))
                .when(taskService).deleteTask(userId, taskId);

        // Act & Assert
        mockMvc.perform(delete("/api/tasks/{taskId}", taskId)
                .with(user(authenticatedUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/tasks/filter - Should filter tasks successfully")
    @WithMockUser
    void filterTasks_Success() throws Exception {
        // Arrange
        TaskFilterRequest filter = TaskFilterRequest.builder()
                .projectId(projectId)
                .statuses(List.of(TaskStatus.TODO))
                .build();

        when(taskService.filterTasks(eq(userId), any(TaskFilterRequest.class)))
                .thenReturn(List.of(taskResponse));

        // Act & Assert
        mockMvc.perform(post("/api/tasks/filter")
                .with(user(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1));

        verify(taskService).filterTasks(eq(userId), any(TaskFilterRequest.class));
    }

    @Test
    @DisplayName("POST /api/tasks/filter - Should filter by multiple criteria")
    @WithMockUser
    void filterTasks_MultipleCriteria() throws Exception {
        // Arrange
        TaskFilterRequest filter = TaskFilterRequest.builder()
                .projectId(projectId)
                .statuses(List.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS))
                .priorities(List.of(TaskPriority.HIGH, TaskPriority.URGENT))
                .searchText("important")
                .build();

        when(taskService.filterTasks(eq(userId), any(TaskFilterRequest.class)))
                .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(post("/api/tasks/filter")
                .with(user(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(taskService).filterTasks(eq(userId), any(TaskFilterRequest.class));
    }

    @Test
    @DisplayName("GET /api/projects/{projectId}/tasks/statistics - Should get task statistics")
    @WithMockUser
    void getProjectStatistics_Success() throws Exception {
        // Arrange
        TaskStatisticsResponse statistics = TaskStatisticsResponse.builder()
                .totalTasks(10L)
                .todoCount(3L)
                .inProgressCount(4L)
                .doneCount(3L)
                .overdueCount(1L)
                .completionRate(new BigDecimal("30.00"))
                .onTimeCompletionRate(new BigDecimal("100.00"))
                .build();

        when(taskService.getProjectStatistics(userId, projectId)).thenReturn(statistics);

        // Act & Assert
        mockMvc.perform(get("/api/projects/{projectId}/tasks/statistics", projectId)
                .with(user(authenticatedUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalTasks").value(10))
                .andExpect(jsonPath("$.todoCount").value(3))
                .andExpect(jsonPath("$.completionRate").value(30.00));

        verify(taskService).getProjectStatistics(userId, projectId);
    }

    @Test
    @DisplayName("POST /api/tasks/{taskId}/comments - Should add comment successfully")
    @WithMockUser
    void addComment_Success() throws Exception {
        // Arrange
        CreateTaskCommentRequest request = CreateTaskCommentRequest.builder()
                .commentText("This is a comment")
                .build();

        TaskCommentResponse commentResponse = TaskCommentResponse.builder()
                .id(UUID.randomUUID())
                .taskId(taskId)
                .userId(userId)
                .userEmail("user@example.com")
                .userName("Test User")
                .commentText("This is a comment")
                .createdAt(LocalDateTime.now())
                .build();

        when(taskService.addComment(eq(userId), eq(taskId), any(CreateTaskCommentRequest.class)))
                .thenReturn(commentResponse);

        // Act & Assert
        mockMvc.perform(post("/api/tasks/{taskId}/comments", taskId)
                .with(user(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.commentText").value("This is a comment"));

        verify(taskService).addComment(eq(userId), eq(taskId), any(CreateTaskCommentRequest.class));
    }

    @Test
    @DisplayName("POST /api/tasks/{taskId}/comments - Should return 400 when comment text is missing")
    @WithMockUser
    void addComment_MissingText_BadRequest() throws Exception {
        // Arrange
        CreateTaskCommentRequest request = CreateTaskCommentRequest.builder().build();

        // Act & Assert
        mockMvc.perform(post("/api/tasks/{taskId}/comments", taskId)
                .with(user(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).addComment(any(), any(), any());
    }

    @Test
    @DisplayName("GET /api/tasks/{taskId}/comments - Should get task comments")
    @WithMockUser
    void getTaskComments_Success() throws Exception {
        // Arrange
        TaskCommentResponse comment1 = TaskCommentResponse.builder()
                .id(UUID.randomUUID())
                .taskId(taskId)
                .userId(userId)
                .userEmail("user@example.com")
                .userName("Test User")
                .commentText("Comment 1")
                .createdAt(LocalDateTime.now())
                .build();

        TaskCommentResponse comment2 = TaskCommentResponse.builder()
                .id(UUID.randomUUID())
                .taskId(taskId)
                .userId(userId)
                .userEmail("user@example.com")
                .userName("Test User")
                .commentText("Comment 2")
                .createdAt(LocalDateTime.now())
                .build();

        when(taskService.getTaskComments(userId, taskId)).thenReturn(List.of(comment1, comment2));

        // Act & Assert
        mockMvc.perform(get("/api/tasks/{taskId}/comments", taskId)
                .with(user(authenticatedUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].commentText").value("Comment 1"))
                .andExpect(jsonPath("$[1].commentText").value("Comment 2"));

        verify(taskService).getTaskComments(userId, taskId);
    }

    @Test
    @DisplayName("GET /api/tasks/{taskId}/comments - Should return empty list when no comments")
    @WithMockUser
    void getTaskComments_EmptyList() throws Exception {
        // Arrange
        when(taskService.getTaskComments(userId, taskId)).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/tasks/{taskId}/comments", taskId)
                .with(user(authenticatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/tasks - Should return 401 when not authenticated")
    void getAllUserTasks_NotAuthenticated_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());

        verify(taskService, never()).getAllUserTasks(any());
    }
}
