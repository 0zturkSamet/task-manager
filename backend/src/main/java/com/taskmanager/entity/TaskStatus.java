package com.taskmanager.entity;

/**
 * Enum representing the status of a task in the workflow.
 */
public enum TaskStatus {
    TODO,           // Task is created but not started
    IN_PROGRESS,    // Task is currently being worked on
    IN_REVIEW,      // Task is completed and awaiting review
    DONE,           // Task is completed and approved
    CANCELLED       // Task is cancelled/abandoned
}
