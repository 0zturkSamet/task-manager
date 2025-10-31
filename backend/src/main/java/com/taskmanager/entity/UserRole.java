package com.taskmanager.entity;

/**
 * Enum representing system-wide user roles for authorization.
 * This is different from ProjectRole which is project-specific.
 */
public enum UserRole {
    /**
     * Regular user with standard access to their own projects and tasks
     */
    USER,

    /**
     * System administrator with access to all projects, tasks, and system management features
     */
    ADMIN
}
