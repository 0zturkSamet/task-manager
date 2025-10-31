package com.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCommentResponse {

    private UUID id;
    private UUID taskId;

    // User information
    private UUID userId;
    private String userName;
    private String userEmail;

    private String commentText;
    private Integer likesCount;
    private Integer dislikesCount;

    // Current user's reaction: null, "LIKE", or "DISLIKE"
    private String userReaction;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
