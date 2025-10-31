package com.taskmanager.service;

import com.taskmanager.dto.*;
import com.taskmanager.entity.*;
import com.taskmanager.exception.ForbiddenException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskCommentRepository taskCommentRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private UUID userId;
    private UUID projectId;
    private UUID taskId;
    private Project project;
    private Task task;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .build();

        project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .ownerId(userId)
                .isActive(true)
                .build();

        task = Task.builder()
                .id(taskId)
                .title("Test Task")
                .description("Test Description")
                .projectId(projectId)
                .createdByUserId(userId)
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .position(0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create task successfully")
    void createTask_Success() {
        // Arrange
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("New Task")
                .description("Task Description")
                .projectId(projectId)
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.TODO)
                .build();

        when(projectRepository.findByIdAndIsActiveTrue(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.hasAccess(projectId, userId)).thenReturn(true);
        when(projectRepository.isOwner(projectId, userId)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(taskCommentRepository.countByTaskId(taskId)).thenReturn(0L);

        // Act
        TaskResponse response = taskService.createTask(userId, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo(task.getTitle());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("Should use default values when not provided in create request")
    void createTask_DefaultValues() {
        // Arrange
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("New Task")
                .projectId(projectId)
                .build(); // No priority, status, or position

        when(projectRepository.findByIdAndIsActiveTrue(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.hasAccess(projectId, userId)).thenReturn(true);
        when(projectRepository.isOwner(projectId, userId)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(taskCommentRepository.countByTaskId(any())).thenReturn(0L);

        // Act
        taskService.createTask(userId, request);

        // Assert
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        Task savedTask = taskCaptor.getValue();
        assertThat(savedTask.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(savedTask.getPosition()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should throw exception when user has no access to project")
    void createTask_NoAccess_ThrowsException() {
        // Arrange
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("New Task")
                .projectId(projectId)
                .build();

        when(projectRepository.findByIdAndIsActiveTrue(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.hasAccess(projectId, userId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> taskService.createTask(userId, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You don't have access to this project");
    }

    @Test
    @DisplayName("Should throw exception when member tries to create task")
    void createTask_AsMember_ThrowsException() {
        // Arrange
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("New Task")
                .projectId(projectId)
                .build();

        when(projectRepository.findByIdAndIsActiveTrue(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.hasAccess(projectId, userId)).thenReturn(true);
        when(projectRepository.isOwner(projectId, userId)).thenReturn(false);
        when(projectMemberRepository.findRoleByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.of(ProjectRole.MEMBER));

        // Act & Assert - Only OWNER and ADMIN can create tasks
        assertThatThrownBy(() -> taskService.createTask(userId, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You don't have permission to create tasks in this project");
    }

    @Test
    @DisplayName("Should validate assignment when assigning task to user")
    void createTask_ValidateAssignment() {
        // Arrange
        UUID assigneeId = UUID.randomUUID();
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("New Task")
                .projectId(projectId)
                .assignedToId(assigneeId)
                .build();

        when(projectRepository.findByIdAndIsActiveTrue(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.hasAccess(projectId, userId)).thenReturn(true);
        when(projectRepository.isOwner(projectId, userId)).thenReturn(true);
        when(projectMemberRepository.existsByProjectIdAndUserId(projectId, assigneeId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> taskService.createTask(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot assign task to user who is not a project member");
    }

    @Test
    @DisplayName("Should update task status and set completion timestamp")
    void updateTask_StatusToDone_SetsCompletionTime() {
        // Arrange
        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .status(TaskStatus.DONE)
                .build();

        task.setCompletedAt(null); // Initially not completed

        when(taskRepository.findByIdAndIsActiveTrue(taskId)).thenReturn(Optional.of(task));
        when(projectRepository.isOwner(projectId, userId)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(taskCommentRepository.countByTaskId(taskId)).thenReturn(0L);

        // Act
        taskService.updateTask(userId, taskId, request);

        // Assert
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        Task updatedTask = taskCaptor.getValue();
        assertThat(updatedTask.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(updatedTask.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should clear completion timestamp when unmarking as done")
    void updateTask_StatusFromDone_ClearsCompletionTime() {
        // Arrange
        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .status(TaskStatus.IN_PROGRESS)
                .build();

        task.setStatus(TaskStatus.DONE);
        task.setCompletedAt(LocalDateTime.now());

        when(taskRepository.findByIdAndIsActiveTrue(taskId)).thenReturn(Optional.of(task));
        when(projectRepository.isOwner(projectId, userId)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(taskCommentRepository.countByTaskId(taskId)).thenReturn(0L);

        // Act
        taskService.updateTask(userId, taskId, request);

        // Assert
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        Task updatedTask = taskCaptor.getValue();
        assertThat(updatedTask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(updatedTask.getCompletedAt()).isNull();
    }

    @Test
    @DisplayName("Should throw exception when member tries to edit task")
    void updateTask_AsMember_ThrowsException() {
        // Arrange
        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .title("Updated Task")
                .build();

        when(taskRepository.findByIdAndIsActiveTrue(taskId)).thenReturn(Optional.of(task));
        when(projectRepository.isOwner(projectId, userId)).thenReturn(false);
        when(projectMemberRepository.findRoleByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.of(ProjectRole.MEMBER));

        // Act & Assert - Only OWNER and ADMIN can edit tasks
        assertThatThrownBy(() -> taskService.updateTask(userId, taskId, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You don't have permission to edit tasks in this project");
    }

    @Test
    @DisplayName("Should delete task when user is project owner")
    void deleteTask_AsOwner_Success() {
        // Arrange
        when(taskRepository.findByIdAndIsActiveTrue(taskId)).thenReturn(Optional.of(task));
        when(projectRepository.isOwner(projectId, userId)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        taskService.deleteTask(userId, taskId);

        // Assert
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        assertThat(taskCaptor.getValue().getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should delete task when user is task creator")
    void deleteTask_AsCreator_Success() {
        // Arrange
        when(taskRepository.findByIdAndIsActiveTrue(taskId)).thenReturn(Optional.of(task));
        when(projectRepository.isOwner(projectId, userId)).thenReturn(false);
        // task.createdByUserId is already set to userId in setUp

        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        taskService.deleteTask(userId, taskId);

        // Assert
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("Should throw exception when non-owner/non-creator tries to delete task")
    void deleteTask_NoPermission_ThrowsException() {
        // Arrange
        UUID otherUserId = UUID.randomUUID();
        task.setCreatedByUserId(otherUserId); // Different creator

        when(taskRepository.findByIdAndIsActiveTrue(taskId)).thenReturn(Optional.of(task));
        when(projectRepository.isOwner(projectId, userId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> taskService.deleteTask(userId, taskId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Only project owner or task creator can delete tasks");
    }

    @Test
    @DisplayName("Should filter tasks by status")
    void filterTasks_ByStatus() {
        // Arrange
        Task task1 = createTaskWithStatus(TaskStatus.TODO);
        Task task2 = createTaskWithStatus(TaskStatus.IN_PROGRESS);
        Task task3 = createTaskWithStatus(TaskStatus.DONE);

        TaskFilterRequest filter = TaskFilterRequest.builder()
                .projectId(projectId)
                .statuses(List.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS))
                .build();

        when(projectRepository.hasAccess(projectId, userId)).thenReturn(true);
        when(taskRepository.findByProjectIdAndIsActiveTrue(projectId))
                .thenReturn(Arrays.asList(task1, task2, task3));
        setupTaskResponseMocks();

        // Act
        List<TaskResponse> result = taskService.filterTasks(userId, filter);

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should filter tasks by priority")
    void filterTasks_ByPriority() {
        // Arrange
        Task task1 = createTaskWithPriority(TaskPriority.LOW);
        Task task2 = createTaskWithPriority(TaskPriority.HIGH);
        Task task3 = createTaskWithPriority(TaskPriority.URGENT);

        TaskFilterRequest filter = TaskFilterRequest.builder()
                .projectId(projectId)
                .priorities(List.of(TaskPriority.HIGH, TaskPriority.URGENT))
                .build();

        when(projectRepository.hasAccess(projectId, userId)).thenReturn(true);
        when(taskRepository.findByProjectIdAndIsActiveTrue(projectId))
                .thenReturn(Arrays.asList(task1, task2, task3));
        setupTaskResponseMocks();

        // Act
        List<TaskResponse> result = taskService.filterTasks(userId, filter);

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should filter overdue tasks")
    void filterTasks_Overdue() {
        // Arrange
        Task overdueTask = createTaskWithDueDate(LocalDateTime.now().minusDays(1));
        overdueTask.setStatus(TaskStatus.IN_PROGRESS);

        Task notOverdueTask = createTaskWithDueDate(LocalDateTime.now().plusDays(1));
        notOverdueTask.setStatus(TaskStatus.IN_PROGRESS);

        Task completedOverdueTask = createTaskWithDueDate(LocalDateTime.now().minusDays(1));
        completedOverdueTask.setStatus(TaskStatus.DONE);

        TaskFilterRequest filter = TaskFilterRequest.builder()
                .projectId(projectId)
                .overdue(true)
                .build();

        when(projectRepository.hasAccess(projectId, userId)).thenReturn(true);
        when(taskRepository.findByProjectIdAndIsActiveTrue(projectId))
                .thenReturn(Arrays.asList(overdueTask, notOverdueTask, completedOverdueTask));
        setupTaskResponseMocks();

        // Act
        List<TaskResponse> result = taskService.filterTasks(userId, filter);

        // Assert
        assertThat(result).hasSize(1); // Only the overdue in-progress task
    }

    @Test
    @DisplayName("Should filter tasks by search text")
    void filterTasks_BySearchText() {
        // Arrange
        Task task1 = createTaskWithTitle("Bug fix in authentication");
        Task task2 = createTaskWithTitle("Feature: Add dark mode");
        Task task3 = createTaskWithTitle("Update documentation");
        task3.setDescription("Fix authentication docs");

        TaskFilterRequest filter = TaskFilterRequest.builder()
                .projectId(projectId)
                .searchText("authentication")
                .build();

        when(projectRepository.hasAccess(projectId, userId)).thenReturn(true);
        when(taskRepository.findByProjectIdAndIsActiveTrue(projectId))
                .thenReturn(Arrays.asList(task1, task2, task3));
        setupTaskResponseMocks();

        // Act
        List<TaskResponse> result = taskService.filterTasks(userId, filter);

        // Assert
        assertThat(result).hasSize(2); // task1 (title) and task3 (description)
    }

    @Test
    @DisplayName("Should calculate project statistics correctly")
    void getProjectStatistics_Success() {
        // Arrange
        Task todoTask = createTaskWithStatus(TaskStatus.TODO);
        Task inProgressTask = createTaskWithStatus(TaskStatus.IN_PROGRESS);
        Task doneTask = createTaskWithStatus(TaskStatus.DONE);
        doneTask.setDueDate(LocalDateTime.now().plusDays(1));
        doneTask.setCompletedAt(LocalDateTime.now());

        Task overdueTask = createTaskWithDueDate(LocalDateTime.now().minusDays(1));
        overdueTask.setStatus(TaskStatus.TODO);

        when(projectRepository.hasAccess(projectId, userId)).thenReturn(true);
        when(taskRepository.findAllActiveTasksByProjectId(projectId))
                .thenReturn(Arrays.asList(todoTask, inProgressTask, doneTask, overdueTask));

        // Act
        TaskStatisticsResponse stats = taskService.getProjectStatistics(userId, projectId);

        // Assert
        assertThat(stats.getTotalTasks()).isEqualTo(4);
        assertThat(stats.getTodoCount()).isEqualTo(2); // todoTask + overdueTask
        assertThat(stats.getInProgressCount()).isEqualTo(1);
        assertThat(stats.getDoneCount()).isEqualTo(1);
        assertThat(stats.getOverdueCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should calculate completion rate correctly")
    void getProjectStatistics_CompletionRate() {
        // Arrange
        Task task1 = createTaskWithStatus(TaskStatus.DONE);
        Task task2 = createTaskWithStatus(TaskStatus.DONE);
        Task task3 = createTaskWithStatus(TaskStatus.TODO);
        Task task4 = createTaskWithStatus(TaskStatus.IN_PROGRESS);

        when(projectRepository.hasAccess(projectId, userId)).thenReturn(true);
        when(taskRepository.findAllActiveTasksByProjectId(projectId))
                .thenReturn(Arrays.asList(task1, task2, task3, task4));

        // Act
        TaskStatisticsResponse stats = taskService.getProjectStatistics(userId, projectId);

        // Assert
        assertThat(stats.getCompletionRate()).isEqualTo(new BigDecimal("50.00")); // 2 done out of 4 = 50%
    }

    @Test
    @DisplayName("Should calculate on-time completion rate")
    void getProjectStatistics_OnTimeCompletionRate() {
        // Arrange
        // Completed on time
        Task onTimeTask = createTaskWithStatus(TaskStatus.DONE);
        onTimeTask.setDueDate(LocalDateTime.now().plusDays(1));
        onTimeTask.setCompletedAt(LocalDateTime.now());

        // Completed late
        Task lateTask = createTaskWithStatus(TaskStatus.DONE);
        lateTask.setDueDate(LocalDateTime.now().minusDays(1));
        lateTask.setCompletedAt(LocalDateTime.now());

        when(projectRepository.hasAccess(projectId, userId)).thenReturn(true);
        when(taskRepository.findAllActiveTasksByProjectId(projectId))
                .thenReturn(Arrays.asList(onTimeTask, lateTask));

        // Act
        TaskStatisticsResponse stats = taskService.getProjectStatistics(userId, projectId);

        // Assert
        assertThat(stats.getOnTimeCompletionRate()).isEqualTo(new BigDecimal("50.00")); // 1 on time out of 2 = 50%
    }

    @Test
    @DisplayName("Should add comment to task")
    void addComment_Success() {
        // Arrange
        CreateTaskCommentRequest request = CreateTaskCommentRequest.builder()
                .commentText("This is a comment")
                .build();

        TaskComment comment = TaskComment.builder()
                .id(UUID.randomUUID())
                .taskId(taskId)
                .userId(userId)
                .commentText("This is a comment")
                .createdAt(LocalDateTime.now())
                .build();

        when(taskRepository.findByIdAndIsActiveTrue(taskId)).thenReturn(Optional.of(task));
        when(projectRepository.hasAccess(projectId, userId)).thenReturn(true);
        when(taskCommentRepository.save(any(TaskComment.class))).thenReturn(comment);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        TaskCommentResponse response = taskService.addComment(userId, taskId, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCommentText()).isEqualTo("This is a comment");
        verify(taskCommentRepository).save(any(TaskComment.class));
    }

    @Test
    @DisplayName("Should get task comments")
    void getTaskComments_Success() {
        // Arrange
        TaskComment comment1 = TaskComment.builder()
                .id(UUID.randomUUID())
                .taskId(taskId)
                .userId(userId)
                .commentText("Comment 1")
                .createdAt(LocalDateTime.now())
                .build();

        TaskComment comment2 = TaskComment.builder()
                .id(UUID.randomUUID())
                .taskId(taskId)
                .userId(userId)
                .commentText("Comment 2")
                .createdAt(LocalDateTime.now())
                .build();

        when(taskRepository.findByIdAndIsActiveTrue(taskId)).thenReturn(Optional.of(task));
        when(projectRepository.hasAccess(projectId, userId)).thenReturn(true);
        when(taskCommentRepository.findByTaskIdOrderByCreatedAtDesc(taskId))
                .thenReturn(Arrays.asList(comment1, comment2));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        List<TaskCommentResponse> comments = taskService.getTaskComments(userId, taskId);

        // Assert
        assertThat(comments).hasSize(2);
    }

    @Test
    @DisplayName("Should throw exception when adding comment without access")
    void addComment_NoAccess_ThrowsException() {
        // Arrange
        CreateTaskCommentRequest request = CreateTaskCommentRequest.builder()
                .commentText("This is a comment")
                .build();

        when(taskRepository.findByIdAndIsActiveTrue(taskId)).thenReturn(Optional.of(task));
        when(projectRepository.hasAccess(projectId, userId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> taskService.addComment(userId, taskId, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You don't have access to this task");
    }

    // Helper methods
    private Task createTaskWithStatus(TaskStatus status) {
        return Task.builder()
                .id(UUID.randomUUID())
                .title("Task")
                .projectId(projectId)
                .createdByUserId(userId)
                .status(status)
                .priority(TaskPriority.MEDIUM)
                .isActive(true)
                .build();
    }

    private Task createTaskWithPriority(TaskPriority priority) {
        return Task.builder()
                .id(UUID.randomUUID())
                .title("Task")
                .projectId(projectId)
                .createdByUserId(userId)
                .status(TaskStatus.TODO)
                .priority(priority)
                .isActive(true)
                .build();
    }

    private Task createTaskWithDueDate(LocalDateTime dueDate) {
        return Task.builder()
                .id(UUID.randomUUID())
                .title("Task")
                .projectId(projectId)
                .createdByUserId(userId)
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(dueDate)
                .isActive(true)
                .build();
    }

    private Task createTaskWithTitle(String title) {
        return Task.builder()
                .id(UUID.randomUUID())
                .title(title)
                .projectId(projectId)
                .createdByUserId(userId)
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .isActive(true)
                .build();
    }

    private void setupTaskResponseMocks() {
        when(projectRepository.findById(any())).thenReturn(Optional.of(project));
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(taskCommentRepository.countByTaskId(any())).thenReturn(0L);
    }
}
