package com.taskmanager.entity;

/**
 * Enum representing the different roles a user can have in a project.
 */
public enum ProjectRole {
    /**
     * Owner has full control over the project including deletion and member management
     */
    OWNER,

    /**
     * Admin can modify project details, manage tasks, and add/remove members
     */
    ADMIN,

    /**
     * Member can view and modify tasks but cannot manage project settings or members
     */
    MEMBER
}
