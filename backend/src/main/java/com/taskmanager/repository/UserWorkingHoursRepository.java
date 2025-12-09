package com.taskmanager.repository;

import com.taskmanager.entity.UserWorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserWorkingHoursRepository extends JpaRepository<UserWorkingHours, UUID> {
    Optional<UserWorkingHours> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
    void deleteByUserId(UUID userId);
}
