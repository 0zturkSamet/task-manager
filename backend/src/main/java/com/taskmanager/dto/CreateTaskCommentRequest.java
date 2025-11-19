package com.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaskCommentRequest {

    @NotBlank(message = "Comment text is required")
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String commentText;
}
