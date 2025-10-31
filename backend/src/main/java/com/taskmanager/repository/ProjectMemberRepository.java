package com.taskmanager.repository;

import com.taskmanager.entity.ProjectMember;
import com.taskmanager.entity.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    // Find all members of a project
    List<ProjectMember> findByProjectIdOrderByJoinedAtAsc(UUID projectId);

    // Find a specific member in a project
    Optional<ProjectMember> findByProjectIdAndUserId(UUID projectId, UUID userId);

    // Check if user is already a member of a project
    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);

    // Get user's role in a project
    @Query("SELECT pm.role FROM ProjectMember pm " +
           "WHERE pm.projectId = :projectId AND pm.userId = :userId")
    Optional<ProjectRole> findRoleByProjectIdAndUserId(
        @Param("projectId") UUID projectId,
        @Param("userId") UUID userId
    );

    // Delete a member from a project
    void deleteByProjectIdAndUserId(UUID projectId, UUID userId);

    // Count members in a project
    long countByProjectId(UUID projectId);

    // Find all projects for a user
    List<ProjectMember> findByUserId(UUID userId);

    // Count projects where user is a member
    long countByUserId(UUID userId);
}
