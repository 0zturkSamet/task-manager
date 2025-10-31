package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    // Find by ID and active
    Optional<Task> findByIdAndIsActiveTrue(UUID id);

    // Find all tasks for a project
    @Query("SELECT t FROM Task t WHERE t.projectId = :projectId AND t.isActive = true ORDER BY t.position ASC, t.createdAt DESC")
    List<Task> findByProjectIdAndIsActiveTrue(@Param("projectId") UUID projectId);

    // Find all tasks for a user (across all projects they're part of)
    @Query("""
        SELECT DISTINCT t FROM Task t, ProjectMember pm
        WHERE t.projectId = pm.projectId
        AND pm.userId = :userId
        AND t.isActive = true
        ORDER BY t.dueDate ASC NULLS LAST, t.priority DESC, t.createdAt DESC
        """)
    List<Task> findAllUserTasks(@Param("userId") UUID userId);

    // Find all tasks in the system (for admins)
    @Query("SELECT t FROM Task t WHERE t.isActive = true ORDER BY t.dueDate ASC NULLS LAST, t.priority DESC, t.createdAt DESC")
    List<Task> findAllActiveTasks();

    // Find tasks assigned to a user
    List<Task> findByAssignedToIdAndIsActiveTrueOrderByDueDateAscCreatedAtDesc(UUID assignedToId);

    // Find tasks created by a user
    List<Task> findByCreatedByUserIdAndIsActiveTrueOrderByCreatedAtDesc(UUID createdByUserId);

    // Find tasks by status
    @Query("SELECT t FROM Task t WHERE t.projectId = :projectId AND t.status = :status AND t.isActive = true ORDER BY t.position ASC")
    List<Task> findByProjectIdAndStatus(@Param("projectId") UUID projectId, @Param("status") TaskStatus status);

    // Find tasks by priority
    @Query("SELECT t FROM Task t WHERE t.projectId = :projectId AND t.priority = :priority AND t.isActive = true ORDER BY t.dueDate ASC NULLS LAST")
    List<Task> findByProjectIdAndPriority(@Param("projectId") UUID projectId, @Param("priority") TaskPriority priority);

    // Find overdue tasks
    @Query("""
        SELECT t FROM Task t
        WHERE t.projectId = :projectId
        AND t.dueDate < :now
        AND t.status NOT IN ('DONE', 'CANCELLED')
        AND t.isActive = true
        ORDER BY t.dueDate ASC
        """)
    List<Task> findOverdueTasks(@Param("projectId") UUID projectId, @Param("now") LocalDateTime now);

    // Find tasks due today
    @Query("""
        SELECT t FROM Task t
        WHERE t.projectId = :projectId
        AND DATE(t.dueDate) = DATE(:today)
        AND t.status NOT IN ('DONE', 'CANCELLED')
        AND t.isActive = true
        ORDER BY t.priority DESC
        """)
    List<Task> findTasksDueToday(@Param("projectId") UUID projectId, @Param("today") LocalDateTime today);

    // Search tasks by title or description
    @Query("""
        SELECT t FROM Task t
        WHERE t.projectId = :projectId
        AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :searchText, '%'))
             OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchText, '%')))
        AND t.isActive = true
        ORDER BY t.createdAt DESC
        """)
    List<Task> searchTasks(@Param("projectId") UUID projectId, @Param("searchText") String searchText);

    // Count tasks by status for a project
    @Query("SELECT COUNT(t) FROM Task t WHERE t.projectId = :projectId AND t.status = :status AND t.isActive = true")
    Long countByProjectIdAndStatus(@Param("projectId") UUID projectId, @Param("status") TaskStatus status);

    // Count tasks by priority for a project
    @Query("SELECT COUNT(t) FROM Task t WHERE t.projectId = :projectId AND t.priority = :priority AND t.isActive = true")
    Long countByProjectIdAndPriority(@Param("projectId") UUID projectId, @Param("priority") TaskPriority priority);

    // Count overdue tasks
    @Query("""
        SELECT COUNT(t) FROM Task t
        WHERE t.projectId = :projectId
        AND t.dueDate < :now
        AND t.status NOT IN ('DONE', 'CANCELLED')
        AND t.isActive = true
        """)
    Long countOverdueTasks(@Param("projectId") UUID projectId, @Param("now") LocalDateTime now);

    // Count unassigned tasks
    @Query("SELECT COUNT(t) FROM Task t WHERE t.projectId = :projectId AND t.assignedToId IS NULL AND t.isActive = true")
    Long countUnassignedTasks(@Param("projectId") UUID projectId);

    // Get all active tasks for statistics
    @Query("SELECT t FROM Task t WHERE t.projectId = :projectId AND t.isActive = true")
    List<Task> findAllActiveTasksByProjectId(@Param("projectId") UUID projectId);

    // Check if user has access to task (via project membership)
    @Query("""
        SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END
        FROM ProjectMember pm
        INNER JOIN Task t ON pm.projectId = t.projectId
        WHERE t.id = :taskId AND pm.userId = :userId
        """)
    Boolean hasAccessToTask(@Param("taskId") UUID taskId, @Param("userId") UUID userId);
}
