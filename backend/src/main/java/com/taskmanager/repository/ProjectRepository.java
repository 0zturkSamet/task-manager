package com.taskmanager.repository;

import com.taskmanager.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    // Find all active projects owned by a user
    List<Project> findByOwnerIdAndIsActiveTrueOrderByCreatedAtDesc(UUID ownerId);

    // Count active projects owned by a user
    long countByOwnerIdAndIsActiveTrue(UUID ownerId);

    // Find all active projects in the system (for admins)
    List<Project> findAllByIsActiveTrueOrderByCreatedAtDesc();

    // Count all active projects in the system (for admins)
    long countByIsActiveTrue();

    // Find all projects where user is a member (including owned projects)
    @Query("SELECT DISTINCT p FROM Project p " +
           "LEFT JOIN p.members pm " +
           "WHERE p.isActive = true " +
           "AND (p.ownerId = :userId OR pm.userId = :userId) " +
           "ORDER BY p.createdAt DESC")
    List<Project> findAllUserProjects(@Param("userId") UUID userId);

    // Find project by ID and check if it's active
    Optional<Project> findByIdAndIsActiveTrue(UUID id);

    // Check if user is owner of a project
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM Project p " +
           "WHERE p.id = :projectId AND p.ownerId = :userId")
    boolean isOwner(@Param("projectId") UUID projectId, @Param("userId") UUID userId);

    // Check if user has access to a project (owner or member)
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM Project p " +
           "LEFT JOIN p.members pm " +
           "WHERE p.id = :projectId " +
           "AND (p.ownerId = :userId OR pm.userId = :userId)")
    boolean hasAccess(@Param("projectId") UUID projectId, @Param("userId") UUID userId);
}
