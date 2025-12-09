package com.taskmanager.repository;

import com.taskmanager.entity.UserWorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserWorkingHoursRepository extends JpaRepository<UserWorkingHours, String> {
    Optional<UserWorkingHours> findByUserId(String userId);
    boolean existsByUserId(String userId);
    void deleteByUserId(String userId);
}
