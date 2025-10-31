package com.taskmanager.repository;

import com.taskmanager.entity.CommentReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentReactionRepository extends JpaRepository<CommentReaction, UUID> {

    /**
     * Find a reaction by comment ID and user ID
     */
    Optional<CommentReaction> findByCommentIdAndUserId(UUID commentId, UUID userId);

    /**
     * Delete a reaction by comment ID and user ID
     */
    void deleteByCommentIdAndUserId(UUID commentId, UUID userId);

    /**
     * Check if a user has reacted to a comment
     */
    boolean existsByCommentIdAndUserId(UUID commentId, UUID userId);
}
