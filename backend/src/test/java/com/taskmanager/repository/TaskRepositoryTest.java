package com.taskmanager.repository;

import com.taskmanager.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("TaskRepository Integration Tests")
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    private User user1;
    private User user2;
    private Project project1;
    private Project project2;
    private Task todoTask;
    private Task inProgressTask;
    private Task doneTask;
    private Task overdueTask;
    private Task inactiveTask;

    @BeforeEach
    void setUp() {
        // Create users
        user1 = User.builder()
                .email("user1@example.com")
                .password("password")
                .firstName("User")
                .lastName("One")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(user1);

        user2 = User.builder()
                .email("user2@example.com")
                .password("password")
                .firstName("User")
                .lastName("Two")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(user2);

        // Create projects
        project1 = Project.builder()
                .name("Project 1")
                .description("Description 1")
                .color("#FF0000")
                .ownerId(user1.getId())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persist(project1);

        project2 = Project.builder()
                .name("Project 2")
                .description("Description 2")
                .color("#00FF00")
                .ownerId(user2.getId())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persist(project2);

        // Create project members
        ProjectMember member1 = ProjectMember.builder()
                .projectId(project1.getId())
                .userId(user1.getId())
                .role(ProjectRole.OWNER)
                .joinedAt(LocalDateTime.now())
                .build();
        entityManager.persist(member1);

        ProjectMember member2 = ProjectMember.builder()
                .projectId(project1.getId())
                .userId(user2.getId())
                .role(ProjectRole.MEMBER)
                .joinedAt(LocalDateTime.now())
                .build();
        entityManager.persist(member2);

        ProjectMember member3 = ProjectMember.builder()
                .projectId(project2.getId())
                .userId(user2.getId())
                .role(ProjectRole.OWNER)
                .joinedAt(LocalDateTime.now())
                .build();
        entityManager.persist(member3);

        // Create tasks
        todoTask = Task.builder()
                .title("TODO Task")
                .description("TODO Description")
                .projectId(project1.getId())
                .createdByUserId(user1.getId())
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .position(0)
                .isActive(true)
                .createdAt(LocalDateTime.now().minusDays(3))
                .updatedAt(LocalDateTime.now().minusDays(3))
                .build();
        entityManager.persist(todoTask);

        inProgressTask = Task.builder()
                .title("In Progress Task")
                .description("In Progress Description")
                .projectId(project1.getId())
                .createdByUserId(user1.getId())
                .assignedToId(user2.getId())
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .position(1)
                .dueDate(LocalDateTime.now().plusDays(2))
                .isActive(true)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(2))
                .build();
        entityManager.persist(inProgressTask);

        doneTask = Task.builder()
                .title("Done Task")
                .description("Done Description")
                .projectId(project1.getId())
                .createdByUserId(user2.getId())
                .status(TaskStatus.DONE)
                .priority(TaskPriority.LOW)
                .position(2)
                .completedAt(LocalDateTime.now().minusDays(1))
                .isActive(true)
                .createdAt(LocalDateTime.now().minusDays(4))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
        entityManager.persist(doneTask);

        overdueTask = Task.builder()
                .title("Overdue Task")
                .description("Overdue Description")
                .projectId(project1.getId())
                .createdByUserId(user1.getId())
                .status(TaskStatus.TODO)
                .priority(TaskPriority.URGENT)
                .position(3)
                .dueDate(LocalDateTime.now().minusDays(1))
                .isActive(true)
                .createdAt(LocalDateTime.now().minusDays(5))
                .updatedAt(LocalDateTime.now().minusDays(5))
                .build();
        entityManager.persist(overdueTask);

        inactiveTask = Task.builder()
                .title("Inactive Task")
                .description("Inactive Description")
                .projectId(project1.getId())
                .createdByUserId(user1.getId())
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .position(4)
                .isActive(false)
                .createdAt(LocalDateTime.now().minusDays(6))
                .updatedAt(LocalDateTime.now().minusDays(6))
                .build();
        entityManager.persist(inactiveTask);

        entityManager.flush();
    }

    @Test
    @DisplayName("Should find task by ID when active")
    void findByIdAndIsActiveTrue_Success() {
        // Act
        Optional<Task> found = taskRepository.findByIdAndIsActiveTrue(todoTask.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("TODO Task");
    }

    @Test
    @DisplayName("Should not find inactive task by ID")
    void findByIdAndIsActiveTrue_InactiveTask() {
        // Act
        Optional<Task> found = taskRepository.findByIdAndIsActiveTrue(inactiveTask.getId());

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find tasks by project ID ordered by position")
    void findByProjectIdAndIsActiveTrue() {
        // Act
        List<Task> tasks = taskRepository.findByProjectIdAndIsActiveTrue(project1.getId());

        // Assert
        assertThat(tasks).hasSize(4); // All active tasks for project1
        assertThat(tasks).extracting(Task::getTitle)
                .containsExactly("TODO Task", "In Progress Task", "Done Task", "Overdue Task");
    }

    @Test
    @DisplayName("Should return empty list for project with no tasks")
    void findByProjectIdAndIsActiveTrue_NoTasks() {
        // Act
        List<Task> tasks = taskRepository.findByProjectIdAndIsActiveTrue(project2.getId());

        // Assert
        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("Should find all user tasks across projects")
    void findAllUserTasks() {
        // Act
        List<Task> user1Tasks = taskRepository.findAllUserTasks(user1.getId());
        List<Task> user2Tasks = taskRepository.findAllUserTasks(user2.getId());

        // Assert
        assertThat(user1Tasks).hasSize(4); // user1 is member of project1
        assertThat(user2Tasks).hasSize(4); // user2 is member of both projects
    }

    @Test
    @DisplayName("Should find tasks assigned to user")
    void findByAssignedToIdAndIsActiveTrueOrderByDueDateAscCreatedAtDesc() {
        // Act
        List<Task> assignedTasks = taskRepository
                .findByAssignedToIdAndIsActiveTrueOrderByDueDateAscCreatedAtDesc(user2.getId());

        // Assert
        assertThat(assignedTasks).hasSize(1);
        assertThat(assignedTasks.get(0).getTitle()).isEqualTo("In Progress Task");
    }

    @Test
    @DisplayName("Should find tasks created by user")
    void findByCreatedByUserIdAndIsActiveTrueOrderByCreatedAtDesc() {
        // Act
        List<Task> createdTasks = taskRepository
                .findByCreatedByUserIdAndIsActiveTrueOrderByCreatedAtDesc(user1.getId());

        // Assert
        assertThat(createdTasks).hasSize(3); // user1 created 3 active tasks
        assertThat(createdTasks).extracting(Task::getCreatedByUserId)
                .allMatch(id -> id.equals(user1.getId()));
    }

    @Test
    @DisplayName("Should find tasks by project and status")
    void findByProjectIdAndStatus() {
        // Act
        List<Task> todoTasks = taskRepository.findByProjectIdAndStatus(project1.getId(), TaskStatus.TODO);
        List<Task> inProgressTasks = taskRepository.findByProjectIdAndStatus(project1.getId(), TaskStatus.IN_PROGRESS);
        List<Task> doneTasks = taskRepository.findByProjectIdAndStatus(project1.getId(), TaskStatus.DONE);

        // Assert
        assertThat(todoTasks).hasSize(2); // todoTask and overdueTask
        assertThat(inProgressTasks).hasSize(1);
        assertThat(doneTasks).hasSize(1);
    }

    @Test
    @DisplayName("Should find tasks by project and priority")
    void findByProjectIdAndPriority() {
        // Act
        List<Task> urgentTasks = taskRepository.findByProjectIdAndPriority(project1.getId(), TaskPriority.URGENT);
        List<Task> highTasks = taskRepository.findByProjectIdAndPriority(project1.getId(), TaskPriority.HIGH);
        List<Task> mediumTasks = taskRepository.findByProjectIdAndPriority(project1.getId(), TaskPriority.MEDIUM);
        List<Task> lowTasks = taskRepository.findByProjectIdAndPriority(project1.getId(), TaskPriority.LOW);

        // Assert
        assertThat(urgentTasks).hasSize(1);
        assertThat(highTasks).hasSize(1);
        assertThat(mediumTasks).hasSize(1);
        assertThat(lowTasks).hasSize(1);
    }

    @Test
    @DisplayName("Should find overdue tasks")
    void findOverdueTasks() {
        // Act
        List<Task> overdueTasks = taskRepository.findOverdueTasks(project1.getId(), LocalDateTime.now());

        // Assert
        assertThat(overdueTasks).hasSize(1);
        assertThat(overdueTasks.get(0).getTitle()).isEqualTo("Overdue Task");
    }

    @Test
    @DisplayName("Should not include completed tasks in overdue tasks")
    void findOverdueTasks_ExcludesCompletedTasks() {
        // Arrange - Create an overdue but completed task
        Task completedOverdueTask = Task.builder()
                .title("Completed Overdue Task")
                .description("Description")
                .projectId(project1.getId())
                .createdByUserId(user1.getId())
                .status(TaskStatus.DONE)
                .priority(TaskPriority.MEDIUM)
                .position(5)
                .dueDate(LocalDateTime.now().minusDays(2))
                .completedAt(LocalDateTime.now().minusDays(1))
                .isActive(true)
                .createdAt(LocalDateTime.now().minusDays(3))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
        entityManager.persist(completedOverdueTask);
        entityManager.flush();

        // Act
        List<Task> overdueTasks = taskRepository.findOverdueTasks(project1.getId(), LocalDateTime.now());

        // Assert
        assertThat(overdueTasks).hasSize(1);
        assertThat(overdueTasks).noneMatch(t -> t.getStatus() == TaskStatus.DONE);
    }

    @Test
    @DisplayName("Should find tasks due today")
    void findTasksDueToday() {
        // Arrange - Create a task due today
        Task todayTask = Task.builder()
                .title("Today Task")
                .description("Due today")
                .projectId(project1.getId())
                .createdByUserId(user1.getId())
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .position(5)
                .dueDate(LocalDateTime.now())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persist(todayTask);
        entityManager.flush();

        // Act
        List<Task> tasksDueToday = taskRepository.findTasksDueToday(project1.getId(), LocalDateTime.now());

        // Assert
        assertThat(tasksDueToday).hasSizeGreaterThanOrEqualTo(1);
        assertThat(tasksDueToday).anyMatch(t -> t.getTitle().equals("Today Task"));
    }

    @Test
    @DisplayName("Should search tasks by title")
    void searchTasks_ByTitle() {
        // Act
        List<Task> results = taskRepository.searchTasks(project1.getId(), "TODO");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).contains("TODO");
    }

    @Test
    @DisplayName("Should search tasks by description")
    void searchTasks_ByDescription() {
        // Act
        List<Task> results = taskRepository.searchTasks(project1.getId(), "Overdue Description");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDescription()).contains("Overdue Description");
    }

    @Test
    @DisplayName("Should search tasks case-insensitively")
    void searchTasks_CaseInsensitive() {
        // Act
        List<Task> results = taskRepository.searchTasks(project1.getId(), "todo");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).contains("TODO");
    }

    @Test
    @DisplayName("Should return empty list when no tasks match search")
    void searchTasks_NoMatches() {
        // Act
        List<Task> results = taskRepository.searchTasks(project1.getId(), "nonexistent");

        // Assert
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should count tasks by status")
    void countByProjectIdAndStatus() {
        // Act
        Long todoCount = taskRepository.countByProjectIdAndStatus(project1.getId(), TaskStatus.TODO);
        Long inProgressCount = taskRepository.countByProjectIdAndStatus(project1.getId(), TaskStatus.IN_PROGRESS);
        Long doneCount = taskRepository.countByProjectIdAndStatus(project1.getId(), TaskStatus.DONE);

        // Assert
        assertThat(todoCount).isEqualTo(2);
        assertThat(inProgressCount).isEqualTo(1);
        assertThat(doneCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Should count tasks by priority")
    void countByProjectIdAndPriority() {
        // Act
        Long urgentCount = taskRepository.countByProjectIdAndPriority(project1.getId(), TaskPriority.URGENT);
        Long highCount = taskRepository.countByProjectIdAndPriority(project1.getId(), TaskPriority.HIGH);
        Long mediumCount = taskRepository.countByProjectIdAndPriority(project1.getId(), TaskPriority.MEDIUM);
        Long lowCount = taskRepository.countByProjectIdAndPriority(project1.getId(), TaskPriority.LOW);

        // Assert
        assertThat(urgentCount).isEqualTo(1);
        assertThat(highCount).isEqualTo(1);
        assertThat(mediumCount).isEqualTo(1);
        assertThat(lowCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Should count overdue tasks")
    void countOverdueTasks() {
        // Act
        Long overdueCount = taskRepository.countOverdueTasks(project1.getId(), LocalDateTime.now());

        // Assert
        assertThat(overdueCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Should count unassigned tasks")
    void countUnassignedTasks() {
        // Act
        Long unassignedCount = taskRepository.countUnassignedTasks(project1.getId());

        // Assert
        assertThat(unassignedCount).isEqualTo(3); // Only inProgressTask is assigned
    }

    @Test
    @DisplayName("Should find all active tasks for statistics")
    void findAllActiveTasksByProjectId() {
        // Act
        List<Task> tasks = taskRepository.findAllActiveTasksByProjectId(project1.getId());

        // Assert
        assertThat(tasks).hasSize(4);
        assertThat(tasks).allMatch(Task::getIsActive);
    }

    @Test
    @DisplayName("Should check if user has access to task via project membership")
    void hasAccessToTask_True() {
        // Act
        Boolean hasAccess = taskRepository.hasAccessToTask(todoTask.getId(), user1.getId());

        // Assert
        assertThat(hasAccess).isTrue();
    }

    @Test
    @DisplayName("Should return true for member with access to task")
    void hasAccessToTask_AsMember() {
        // Act
        Boolean hasAccess = taskRepository.hasAccessToTask(todoTask.getId(), user2.getId());

        // Assert
        assertThat(hasAccess).isTrue();
    }

    @Test
    @DisplayName("Should return false when user has no access to task")
    void hasAccessToTask_NoAccess() {
        // Arrange
        User user3 = User.builder()
                .email("user3@example.com")
                .password("password")
                .firstName("User")
                .lastName("Three")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(user3);
        entityManager.flush();

        // Act
        Boolean hasAccess = taskRepository.hasAccessToTask(todoTask.getId(), user3.getId());

        // Assert
        assertThat(hasAccess).isFalse();
    }

    @Test
    @DisplayName("Should order tasks by due date in findAllUserTasks")
    void findAllUserTasks_OrderedByDueDate() {
        // Act
        List<Task> tasks = taskRepository.findAllUserTasks(user1.getId());

        // Assert
        assertThat(tasks).isNotEmpty();
        // Tasks with due dates should come first, ordered ascending
        // Then tasks without due dates
        List<Task> tasksWithDueDate = tasks.stream()
                .filter(t -> t.getDueDate() != null)
                .toList();

        if (tasksWithDueDate.size() > 1) {
            for (int i = 0; i < tasksWithDueDate.size() - 1; i++) {
                assertThat(tasksWithDueDate.get(i).getDueDate())
                        .isBeforeOrEqualTo(tasksWithDueDate.get(i + 1).getDueDate());
            }
        }
    }

    @Test
    @DisplayName("Should only return active tasks in search")
    void searchTasks_OnlyActiveTasksReturned() {
        // Act
        List<Task> results = taskRepository.searchTasks(project1.getId(), "Task");

        // Assert
        assertThat(results).allMatch(Task::getIsActive);
        assertThat(results).noneMatch(t -> t.getId().equals(inactiveTask.getId()));
    }
}
