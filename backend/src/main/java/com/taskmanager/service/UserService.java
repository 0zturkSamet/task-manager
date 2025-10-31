package com.taskmanager.service;

import com.taskmanager.dto.UpdateUserRequest;
import com.taskmanager.dto.UserResponse;
import com.taskmanager.dto.UserStatisticsResponse;
import com.taskmanager.entity.*;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.repository.ProjectMemberRepository;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateUserProfile(UUID userId, UpdateUserRequest request) {
        log.info("Updating user profile for ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getProfileImage() != null) {
            user.setProfileImage(request.getProfileImage());
        }

        User updatedUser = userRepository.save(user);
        log.info("User profile updated successfully for ID: {}", userId);

        return mapToUserResponse(updatedUser);
    }

    @Transactional
    public void deleteUserAccount(UUID userId) {
        log.info("Soft deleting user account for ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsActive(false);
        userRepository.save(user);

        log.info("User account soft deleted for ID: {}", userId);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all active users");

        List<User> users = userRepository.findByIsActiveTrue();
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String searchTerm) {
        log.info("Searching users with term: {}", searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return List.of();
        }

        List<User> users = userRepository.searchUsers(searchTerm.trim());
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserStatisticsResponse getUserStatistics(UUID userId) {
        log.info("Calculating statistics for user ID: {}", userId);

        // Verify user exists and check if admin
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LocalDateTime now = LocalDateTime.now();

        long ownedProjects;
        long memberProjects;
        long totalProjects;
        List<Task> allTasks;
        long totalTasks;

        // If admin, return system-wide statistics
        if (user.isAdmin()) {
            log.info("User is admin - calculating system-wide statistics");

            // All projects in the system
            totalProjects = projectRepository.countByIsActiveTrue();
            ownedProjects = projectRepository.countByOwnerIdAndIsActiveTrue(userId);
            memberProjects = totalProjects - ownedProjects;

            // All tasks in the system
            allTasks = taskRepository.findAllActiveTasks();
            totalTasks = allTasks.size();
        } else {
            // Regular user - only their projects and tasks
            ownedProjects = projectRepository.countByOwnerIdAndIsActiveTrue(userId);
            memberProjects = projectMemberRepository.countByUserId(userId);
            totalProjects = ownedProjects + memberProjects;

            // Get all tasks across user's projects
            allTasks = taskRepository.findAllUserTasks(userId);
            totalTasks = allTasks.size();
        }

        // Tasks assigned to me
        List<Task> myTasks = allTasks.stream()
                .filter(t -> userId.equals(t.getAssignedToId()))
                .collect(Collectors.toList());

        // Count by status
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
        long assignedToMeCount = myTasks.size();
        long createdByMeCount = allTasks.stream().filter(t -> userId.equals(t.getCreatedByUserId())).count();

        // Completion metrics
        BigDecimal completionRate = totalTasks > 0 ?
                BigDecimal.valueOf(doneCount * 100.0 / totalTasks).setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        long myTasksDone = myTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        BigDecimal myTasksCompletionRate = myTasks.size() > 0 ?
                BigDecimal.valueOf(myTasksDone * 100.0 / myTasks.size()).setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // Time tracking - all tasks
        BigDecimal totalEstimatedHours = allTasks.stream()
                .map(Task::getEstimatedHours)
                .filter(h -> h != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalActualHours = allTasks.stream()
                .map(Task::getActualHours)
                .filter(h -> h != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Time tracking - my tasks
        BigDecimal myTasksEstimatedHours = myTasks.stream()
                .map(Task::getEstimatedHours)
                .filter(h -> h != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal myTasksActualHours = myTasks.stream()
                .map(Task::getActualHours)
                .filter(h -> h != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return UserStatisticsResponse.builder()
                .totalProjects(totalProjects)
                .ownedProjects(ownedProjects)
                .memberProjects(memberProjects)
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
                .createdByMeCount(createdByMeCount)
                .completionRate(completionRate)
                .myTasksCompletionRate(myTasksCompletionRate)
                .totalEstimatedHours(totalEstimatedHours)
                .totalActualHours(totalActualHours)
                .myTasksEstimatedHours(myTasksEstimatedHours)
                .myTasksActualHours(myTasksActualHours)
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profileImage(user.getProfileImage())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .isActive(user.getIsActive())
                .build();
    }
}
