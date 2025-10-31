package com.taskmanager.service;

import com.taskmanager.dto.*;
import com.taskmanager.entity.*;
import com.taskmanager.exception.ForbiddenException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final CommentReactionRepository commentReactionRepository;
    private final NotificationService notificationService;

    @Transactional
    public TaskResponse createTask(UUID userId, CreateTaskRequest request) {
        log.info("Creating new task for project ID: {}", request.getProjectId());

        // Verify project exists
        Project project = projectRepository.findByIdAndIsActiveTrue(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Check if user has access to this project
        if (!hasAccessToProject(userId, request.getProjectId())) {
            throw new ForbiddenException("You don't have access to this project");
        }

        // Check if user can create tasks (not VIEWER)
        if (!canCreateTasks(userId, request.getProjectId())) {
            throw new ForbiddenException("You don't have permission to create tasks in this project");
        }

        // If assigning to someone, verify they are a member of the project (or user is admin)
        if (request.getAssignedToId() != null) {
            validateAssignment(request.getProjectId(), request.getAssignedToId(), userId);
        }

        // Create task
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .projectId(request.getProjectId())
                .assignedToId(request.getAssignedToId())
                .createdByUserId(userId)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
                .estimatedHours(request.getEstimatedHours())
                .dueDate(request.getDueDate())
                .position(request.getPosition() != null ? request.getPosition() : 0)
                .isActive(true)
                .build();

        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully with ID: {}", savedTask.getId());

        // Create notification if task is assigned to someone
        if (savedTask.getAssignedToId() != null && !savedTask.getAssignedToId().equals(userId)) {
            String taskTitle = savedTask.getTitle();
            String projectName = project.getName();
            notificationService.createNotification(
                    savedTask.getAssignedToId(),
                    savedTask.getId(),
                    NotificationType.TASK_ASSIGNED,
                    "New Task Assigned",
                    String.format("You have been assigned to task '%s' in project '%s'", taskTitle, projectName)
            );
        }

        return mapToTaskResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllUserTasks(UUID userId) {
        log.info("Fetching all tasks for user ID: {}", userId);
        List<Task> tasks = taskRepository.findAllUserTasks(userId);
        return tasks.stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getProjectTasks(UUID userId, UUID projectId) {
        log.info("Fetching tasks for project ID: {}", projectId);

        // Verify user has access to project
        if (!hasAccessToProject(userId, projectId)) {
            throw new ForbiddenException("You don't have access to this project");
        }

        List<Task> tasks = taskRepository.findByProjectIdAndIsActiveTrue(projectId);
        return tasks.stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID userId, UUID taskId) {
        log.info("Fetching task ID: {}", taskId);

        Task task = taskRepository.findByIdAndIsActiveTrue(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // Verify user has access
        if (!hasAccessToProject(userId, task.getProjectId())) {
            throw new ForbiddenException("You don't have access to this task");
        }

        return mapToTaskResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(UUID userId, UUID taskId, UpdateTaskRequest request) {
        log.info("Updating task ID: {}", taskId);

        Task task = taskRepository.findByIdAndIsActiveTrue(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // Check if user has permission to edit
        if (!canEditTask(userId, task.getProjectId())) {
            throw new ForbiddenException("You don't have permission to edit tasks in this project");
        }

        // Update fields if provided
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
            // If marking as DONE, set completed timestamp
            if (request.getStatus() == TaskStatus.DONE && task.getCompletedAt() == null) {
                task.setCompletedAt(LocalDateTime.now());
            }
            // If unmarking as DONE, clear completed timestamp
            if (request.getStatus() != TaskStatus.DONE && task.getCompletedAt() != null) {
                task.setCompletedAt(null);
            }
        }
        if (request.getAssignedToId() != null) {
            validateAssignment(task.getProjectId(), request.getAssignedToId(), userId);
            UUID oldAssignedToId = task.getAssignedToId();
            task.setAssignedToId(request.getAssignedToId());

            // Create notification if assignee changed and new assignee is not the current user
            if (!request.getAssignedToId().equals(oldAssignedToId) && !request.getAssignedToId().equals(userId)) {
                Project project = projectRepository.findById(task.getProjectId()).orElse(null);
                String projectName = project != null ? project.getName() : "Unknown Project";
                notificationService.createNotification(
                        request.getAssignedToId(),
                        task.getId(),
                        NotificationType.TASK_REASSIGNED,
                        "Task Reassigned to You",
                        String.format("You have been assigned to task '%s' in project '%s'", task.getTitle(), projectName)
                );
            }
        }
        if (request.getEstimatedHours() != null) {
            task.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getActualHours() != null) {
            task.setActualHours(request.getActualHours());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getPosition() != null) {
            task.setPosition(request.getPosition());
        }

        Task updatedTask = taskRepository.save(task);
        log.info("Task updated successfully: {}", taskId);

        return mapToTaskResponse(updatedTask);
    }

    @Transactional
    public void deleteTask(UUID userId, UUID taskId) {
        log.info("Deleting task ID: {}", taskId);

        Task task = taskRepository.findByIdAndIsActiveTrue(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // System admins, project owners, or task creators can delete
        if (!isSystemAdmin(userId) && !isProjectOwner(userId, task.getProjectId()) && !task.getCreatedByUserId().equals(userId)) {
            throw new ForbiddenException("Only system admin, project owner, or task creator can delete tasks");
        }

        // Soft delete
        task.setIsActive(false);
        taskRepository.save(task);

        log.info("Task soft deleted successfully: {}", taskId);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> filterTasks(UUID userId, TaskFilterRequest filter) {
        log.info("Filtering tasks with criteria");

        // Verify user has access to the project
        if (filter.getProjectId() != null && !hasAccessToProject(userId, filter.getProjectId())) {
            throw new ForbiddenException("You don't have access to this project");
        }

        // Start with all tasks for the project or user
        List<Task> tasks;
        if (filter.getProjectId() != null) {
            tasks = taskRepository.findByProjectIdAndIsActiveTrue(filter.getProjectId());
        } else {
            tasks = taskRepository.findAllUserTasks(userId);
        }

        // Apply filters
        if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
            tasks = tasks.stream()
                    .filter(t -> filter.getStatuses().contains(t.getStatus()))
                    .collect(Collectors.toList());
        }
        if (filter.getPriorities() != null && !filter.getPriorities().isEmpty()) {
            tasks = tasks.stream()
                    .filter(t -> filter.getPriorities().contains(t.getPriority()))
                    .collect(Collectors.toList());
        }
        if (filter.getAssignedToId() != null) {
            tasks = tasks.stream()
                    .filter(t -> filter.getAssignedToId().equals(t.getAssignedToId()))
                    .collect(Collectors.toList());
        }
        if (filter.getCreatedByUserId() != null) {
            tasks = tasks.stream()
                    .filter(t -> filter.getCreatedByUserId().equals(t.getCreatedByUserId()))
                    .collect(Collectors.toList());
        }
        if (filter.getDueDateFrom() != null) {
            tasks = tasks.stream()
                    .filter(t -> t.getDueDate() != null && t.getDueDate().isAfter(filter.getDueDateFrom()))
                    .collect(Collectors.toList());
        }
        if (filter.getDueDateTo() != null) {
            tasks = tasks.stream()
                    .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(filter.getDueDateTo()))
                    .collect(Collectors.toList());
        }
        if (filter.getSearchText() != null && !filter.getSearchText().isBlank()) {
            String searchLower = filter.getSearchText().toLowerCase();
            tasks = tasks.stream()
                    .filter(t -> (t.getTitle() != null && t.getTitle().toLowerCase().contains(searchLower)) ||
                            (t.getDescription() != null && t.getDescription().toLowerCase().contains(searchLower)))
                    .collect(Collectors.toList());
        }
        if (Boolean.TRUE.equals(filter.getOverdue())) {
            LocalDateTime now = LocalDateTime.now();
            tasks = tasks.stream()
                    .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(now) &&
                            t.getStatus() != TaskStatus.DONE && t.getStatus() != TaskStatus.CANCELLED)
                    .collect(Collectors.toList());
        }

        return tasks.stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskStatisticsResponse getProjectStatistics(UUID userId, UUID projectId) {
        log.info("Calculating statistics for project ID: {}", projectId);

        // Verify user has access
        if (!hasAccessToProject(userId, projectId)) {
            throw new ForbiddenException("You don't have access to this project");
        }

        List<Task> allTasks = taskRepository.findAllActiveTasksByProjectId(projectId);
        LocalDateTime now = LocalDateTime.now();

        // Count by status
        long totalTasks = allTasks.size();
        long todoCount = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();
        long inProgressCount = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long inReviewCount = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_REVIEW).count();
        long doneCount = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        long cancelledCount = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.CANCELLED).count();

        // Count by priority
        long lowPriorityCount = allTasks.stream().filter(t -> t.getPriority() == TaskPriority.LOW).count();
        long mediumPriorityCount = allTasks.stream().filter(t -> t.getPriority() == TaskPriority.MEDIUM).count();
        long highPriorityCount = allTasks.stream().filter(t -> t.getPriority() == TaskPriority.HIGH).count();
        long urgentPriorityCount = allTasks.stream().filter(t -> t.getPriority() == TaskPriority.URGENT).count();

        // Time-based counts
        long overdueCount = allTasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(now) &&
                        t.getStatus() != TaskStatus.DONE && t.getStatus() != TaskStatus.CANCELLED)
                .count();
        long dueTodayCount = allTasks.stream()
                .filter(t -> t.getDueDate() != null &&
                        t.getDueDate().toLocalDate().equals(now.toLocalDate()) &&
                        t.getStatus() != TaskStatus.DONE && t.getStatus() != TaskStatus.CANCELLED)
                .count();
        long dueThisWeekCount = allTasks.stream()
                .filter(t -> t.getDueDate() != null &&
                        t.getDueDate().isAfter(now) &&
                        t.getDueDate().isBefore(now.plusDays(7)) &&
                        t.getStatus() != TaskStatus.DONE && t.getStatus() != TaskStatus.CANCELLED)
                .count();

        // Assignment counts
        long unassignedCount = allTasks.stream().filter(t -> t.getAssignedToId() == null).count();
        long assignedToMeCount = allTasks.stream().filter(t -> userId.equals(t.getAssignedToId())).count();

        // Completion metrics
        BigDecimal completionRate = totalTasks > 0 ?
                BigDecimal.valueOf(doneCount * 100.0 / totalTasks).setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        long completedOnTime = allTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE &&
                        t.getCompletedAt() != null &&
                        t.getDueDate() != null &&
                        t.getCompletedAt().isBefore(t.getDueDate()))
                .count();
        BigDecimal onTimeCompletionRate = doneCount > 0 ?
                BigDecimal.valueOf(completedOnTime * 100.0 / doneCount).setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // Time tracking
        BigDecimal totalEstimatedHours = allTasks.stream()
                .map(Task::getEstimatedHours)
                .filter(h -> h != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalActualHours = allTasks.stream()
                .map(Task::getActualHours)
                .filter(h -> h != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return TaskStatisticsResponse.builder()
                .totalTasks(totalTasks)
                .todoCount(todoCount)
                .inProgressCount(inProgressCount)
                .inReviewCount(inReviewCount)
                .doneCount(doneCount)
                .cancelledCount(cancelledCount)
                .lowPriorityCount(lowPriorityCount)
                .mediumPriorityCount(mediumPriorityCount)
                .highPriorityCount(highPriorityCount)
                .urgentPriorityCount(urgentPriorityCount)
                .overdueCount(overdueCount)
                .dueTodayCount(dueTodayCount)
                .dueThisWeekCount(dueThisWeekCount)
                .unassignedCount(unassignedCount)
                .assignedToMeCount(assignedToMeCount)
                .completionRate(completionRate)
                .onTimeCompletionRate(onTimeCompletionRate)
                .totalEstimatedHours(totalEstimatedHours)
                .totalActualHours(totalActualHours)
                .build();
    }

    @Transactional
    public TaskCommentResponse addComment(UUID userId, UUID taskId, CreateTaskCommentRequest request) {
        log.info("Adding comment to task ID: {}", taskId);

        Task task = taskRepository.findByIdAndIsActiveTrue(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // Verify user has access
        if (!hasAccessToProject(userId, task.getProjectId())) {
            throw new ForbiddenException("You don't have access to this task");
        }

        TaskComment comment = TaskComment.builder()
                .taskId(taskId)
                .userId(userId)
                .commentText(request.getCommentText())
                .build();

        TaskComment savedComment = taskCommentRepository.save(comment);
        log.info("Comment added successfully to task: {}", taskId);

        return mapToTaskCommentResponse(savedComment, userId);
    }

    @Transactional(readOnly = true)
    public List<TaskCommentResponse> getTaskComments(UUID userId, UUID taskId) {
        log.info("Fetching comments for task ID: {}", taskId);

        Task task = taskRepository.findByIdAndIsActiveTrue(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // Verify user has access
        if (!hasAccessToProject(userId, task.getProjectId())) {
            throw new ForbiddenException("You don't have access to this task");
        }

        List<TaskComment> comments = taskCommentRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
        return comments.stream()
                .map(comment -> mapToTaskCommentResponse(comment, userId))
                .collect(Collectors.toList());
    }

    // Permission helper methods
    private boolean isSystemAdmin(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null && user.isAdmin();
    }

    private boolean hasAccessToProject(UUID userId, UUID projectId) {
        // System admins have access to all projects
        if (isSystemAdmin(userId)) {
            return true;
        }
        return projectRepository.hasAccess(projectId, userId);
    }

    private boolean isProjectOwner(UUID userId, UUID projectId) {
        // System admins are treated as owners for permission purposes
        if (isSystemAdmin(userId)) {
            return true;
        }
        return projectRepository.isOwner(projectId, userId);
    }

    private boolean canCreateTasks(UUID userId, UUID projectId) {
        // System admins can create tasks in any project
        if (isSystemAdmin(userId)) {
            return true;
        }
        if (isProjectOwner(userId, projectId)) {
            return true;
        }
        ProjectRole role = projectMemberRepository.findRoleByProjectIdAndUserId(projectId, userId)
                .orElse(null);
        return role == ProjectRole.OWNER || role == ProjectRole.ADMIN;
    }

    private boolean canEditTask(UUID userId, UUID projectId) {
        return canCreateTasks(userId, projectId);  // Same permissions for create and edit
    }

    private void validateAssignment(UUID projectId, UUID assignedUserId, UUID assigningUserId) {
        // System admins can assign tasks to anyone
        if (isSystemAdmin(assigningUserId)) {
            // Just verify the user exists
            userRepository.findById(assignedUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            return;
        }

        // Regular users can only assign to project members
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, assignedUserId)) {
            throw new IllegalArgumentException("Cannot assign task to user who is not a project member");
        }
    }

    // Mapping methods
    private TaskResponse mapToTaskResponse(Task task) {
        // Get project info
        Project project = projectRepository.findById(task.getProjectId()).orElse(null);

        // Get assigned user info
        User assignedUser = task.getAssignedToId() != null ?
                userRepository.findById(task.getAssignedToId()).orElse(null) : null;

        // Get creator info
        User creator = userRepository.findById(task.getCreatedByUserId()).orElse(null);

        // Get comment count
        Long commentCount = taskCommentRepository.countByTaskId(task.getId());

        // Calculate if overdue
        boolean isOverdue = task.getDueDate() != null &&
                task.getDueDate().isBefore(LocalDateTime.now()) &&
                task.getStatus() != TaskStatus.DONE &&
                task.getStatus() != TaskStatus.CANCELLED;

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .projectId(task.getProjectId())
                .projectName(project != null ? project.getName() : "Unknown")
                .assignedToId(task.getAssignedToId())
                .assignedToName(assignedUser != null ? assignedUser.getFirstName() + " " + assignedUser.getLastName() : null)
                .assignedToEmail(assignedUser != null ? assignedUser.getEmail() : null)
                .createdByUserId(task.getCreatedByUserId())
                .createdByUserName(creator != null ? creator.getFirstName() + " " + creator.getLastName() : "Unknown")
                .createdByUserEmail(creator != null ? creator.getEmail() : null)
                .estimatedHours(task.getEstimatedHours())
                .actualHours(task.getActualHours())
                .dueDate(task.getDueDate())
                .completedAt(task.getCompletedAt())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .position(task.getPosition())
                .isOverdue(isOverdue)
                .commentCount(commentCount)
                .build();
    }

    private TaskCommentResponse mapToTaskCommentResponse(TaskComment comment, UUID currentUserId) {
        User user = userRepository.findById(comment.getUserId()).orElse(null);

        // Look up current user's reaction
        String userReaction = null;
        if (currentUserId != null) {
            Optional<CommentReaction> reaction = commentReactionRepository.findByCommentIdAndUserId(comment.getId(), currentUserId);
            if (reaction.isPresent()) {
                userReaction = reaction.get().getReactionType().name();
            }
        }

        return TaskCommentResponse.builder()
                .id(comment.getId())
                .taskId(comment.getTaskId())
                .userId(comment.getUserId())
                .userName(user != null ? user.getFirstName() + " " + user.getLastName() : "Unknown")
                .userEmail(user != null ? user.getEmail() : null)
                .commentText(comment.getCommentText())
                .likesCount(comment.getLikesCount() != null ? comment.getLikesCount() : 0)
                .dislikesCount(comment.getDislikesCount() != null ? comment.getDislikesCount() : 0)
                .userReaction(userReaction)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    @Transactional
    public TaskCommentResponse likeComment(UUID userId, UUID commentId) {
        log.info("User {} liking comment {}", userId, commentId);

        TaskComment comment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        // Verify user has access to the task
        Task task = taskRepository.findByIdAndIsActiveTrue(comment.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (!hasAccessToProject(userId, task.getProjectId())) {
            throw new ForbiddenException("You don't have access to this comment");
        }

        // Check if user already has a reaction
        Optional<CommentReaction> existingReaction = commentReactionRepository.findByCommentIdAndUserId(commentId, userId);

        if (existingReaction.isPresent()) {
            CommentReaction reaction = existingReaction.get();

            if (reaction.getReactionType() == ReactionType.LIKE) {
                // Toggle: Remove like
                commentReactionRepository.delete(reaction);
                comment.setLikesCount(Math.max(0, comment.getLikesCount() - 1));
                log.info("User {} removed like from comment {}", userId, commentId);
            } else {
                // Switch from dislike to like
                comment.setDislikesCount(Math.max(0, comment.getDislikesCount() - 1));
                reaction.setReactionType(ReactionType.LIKE);
                commentReactionRepository.save(reaction);
                comment.setLikesCount(comment.getLikesCount() + 1);
                log.info("User {} switched from dislike to like on comment {}", userId, commentId);
            }
        } else {
            // New like
            CommentReaction newReaction = CommentReaction.builder()
                    .commentId(commentId)
                    .userId(userId)
                    .reactionType(ReactionType.LIKE)
                    .build();
            commentReactionRepository.save(newReaction);
            comment.setLikesCount(comment.getLikesCount() + 1);
            log.info("User {} liked comment {}", userId, commentId);
        }

        TaskComment updatedComment = taskCommentRepository.save(comment);
        return mapToTaskCommentResponse(updatedComment, userId);
    }

    @Transactional
    public TaskCommentResponse dislikeComment(UUID userId, UUID commentId) {
        log.info("User {} disliking comment {}", userId, commentId);

        TaskComment comment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        // Verify user has access to the task
        Task task = taskRepository.findByIdAndIsActiveTrue(comment.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (!hasAccessToProject(userId, task.getProjectId())) {
            throw new ForbiddenException("You don't have access to this comment");
        }

        // Check if user already has a reaction
        Optional<CommentReaction> existingReaction = commentReactionRepository.findByCommentIdAndUserId(commentId, userId);

        if (existingReaction.isPresent()) {
            CommentReaction reaction = existingReaction.get();

            if (reaction.getReactionType() == ReactionType.DISLIKE) {
                // Toggle: Remove dislike
                commentReactionRepository.delete(reaction);
                comment.setDislikesCount(Math.max(0, comment.getDislikesCount() - 1));
                log.info("User {} removed dislike from comment {}", userId, commentId);
            } else {
                // Switch from like to dislike
                comment.setLikesCount(Math.max(0, comment.getLikesCount() - 1));
                reaction.setReactionType(ReactionType.DISLIKE);
                commentReactionRepository.save(reaction);
                comment.setDislikesCount(comment.getDislikesCount() + 1);
                log.info("User {} switched from like to dislike on comment {}", userId, commentId);
            }
        } else {
            // New dislike
            CommentReaction newReaction = CommentReaction.builder()
                    .commentId(commentId)
                    .userId(userId)
                    .reactionType(ReactionType.DISLIKE)
                    .build();
            commentReactionRepository.save(newReaction);
            comment.setDislikesCount(comment.getDislikesCount() + 1);
            log.info("User {} disliked comment {}", userId, commentId);
        }

        TaskComment updatedComment = taskCommentRepository.save(comment);
        return mapToTaskCommentResponse(updatedComment, userId);
    }
}
