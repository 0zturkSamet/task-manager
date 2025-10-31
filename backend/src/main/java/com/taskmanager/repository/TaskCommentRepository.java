package com.taskmanager.repository;

import com.taskmanager.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, UUID> {

    // Find all comments for a task
    List<TaskComment> findByTaskIdOrderByCreatedAtDesc(UUID taskId);

    // Find all comments by a user
    List<TaskComment> findByUserIdOrderByCreatedAtDesc(UUID userId);

    // Count comments for a task
    Long countByTaskId(UUID taskId);

    // Find recent comments for a task (limit would be in service layer)
    @Query("SELECT tc FROM TaskComment tc WHERE tc.taskId = :taskId ORDER BY tc.createdAt DESC")
    List<TaskComment> findRecentComments(@Param("taskId") UUID taskId);

    // Delete all comments for a task
    void deleteByTaskId(UUID taskId);
}
