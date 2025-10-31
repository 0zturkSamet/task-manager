package com.taskmanager.repository;

import com.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailAndIsActiveTrue(String email);

    List<User> findByIsActiveTrue();

    // Search users by email or name
    @Query("""
        SELECT u FROM User u
        WHERE u.isActive = true
        AND (LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
             OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
             OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        ORDER BY u.firstName ASC, u.lastName ASC
        """)
    List<User> searchUsers(@Param("searchTerm") String searchTerm);
}
