package com.taskmanager.repository;

import com.taskmanager.entity.Project;
import com.taskmanager.entity.ProjectMember;
import com.taskmanager.entity.ProjectRole;
import com.taskmanager.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("ProjectRepository Integration Tests")
class ProjectRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    private User owner;
    private User member1;
    private User member2;
    private Project project1;
    private Project project2;
    private Project inactiveProject;

    @BeforeEach
    void setUp() {
        // Create users
        owner = User.builder()
                .email("owner@example.com")
                .password("password")
                .firstName("Owner")
                .lastName("User")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(owner);

        member1 = User.builder()
                .email("member1@example.com")
                .password("password")
                .firstName("Member")
                .lastName("One")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(member1);

        member2 = User.builder()
                .email("member2@example.com")
                .password("password")
                .firstName("Member")
                .lastName("Two")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(member2);

        // Create projects
        project1 = Project.builder()
                .name("Project 1")
                .description("Description 1")
                .color("#FF0000")
                .ownerId(owner.getId())
                .isActive(true)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(2))
                .build();
        entityManager.persist(project1);

        project2 = Project.builder()
                .name("Project 2")
                .description("Description 2")
                .color("#00FF00")
                .ownerId(owner.getId())
                .isActive(true)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
        entityManager.persist(project2);

        inactiveProject = Project.builder()
                .name("Inactive Project")
                .description("Inactive Description")
                .color("#0000FF")
                .ownerId(owner.getId())
                .isActive(false)
                .createdAt(LocalDateTime.now().minusDays(3))
                .updatedAt(LocalDateTime.now().minusDays(3))
                .build();
        entityManager.persist(inactiveProject);

        // Create project members
        ProjectMember ownerMember1 = ProjectMember.builder()
                .projectId(project1.getId())
                .userId(owner.getId())
                .role(ProjectRole.OWNER)
                .joinedAt(LocalDateTime.now().minusDays(2))
                .build();
        entityManager.persist(ownerMember1);

        ProjectMember member1Project1 = ProjectMember.builder()
                .projectId(project1.getId())
                .userId(member1.getId())
                .role(ProjectRole.MEMBER)
                .joinedAt(LocalDateTime.now().minusDays(1))
                .build();
        entityManager.persist(member1Project1);

        ProjectMember ownerMember2 = ProjectMember.builder()
                .projectId(project2.getId())
                .userId(owner.getId())
                .role(ProjectRole.OWNER)
                .joinedAt(LocalDateTime.now().minusDays(1))
                .build();
        entityManager.persist(ownerMember2);

        entityManager.flush();
    }

    @Test
    @DisplayName("Should find projects by owner ID and active status ordered by creation date desc")
    void findByOwnerIdAndIsActiveTrueOrderByCreatedAtDesc() {
        // Act
        List<Project> projects = projectRepository.findByOwnerIdAndIsActiveTrueOrderByCreatedAtDesc(owner.getId());

        // Assert
        assertThat(projects).hasSize(2);
        assertThat(projects.get(0).getName()).isEqualTo("Project 2"); // Most recent first
        assertThat(projects.get(1).getName()).isEqualTo("Project 1");
        assertThat(projects).noneMatch(p -> !p.getIsActive()); // All should be active
    }

    @Test
    @DisplayName("Should return empty list when owner has no active projects")
    void findByOwnerIdAndIsActiveTrueOrderByCreatedAtDesc_NoProjects() {
        // Arrange
        User newOwner = User.builder()
                .email("newowner@example.com")
                .password("password")
                .firstName("New")
                .lastName("Owner")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(newOwner);
        entityManager.flush();

        // Act
        List<Project> projects = projectRepository.findByOwnerIdAndIsActiveTrueOrderByCreatedAtDesc(newOwner.getId());

        // Assert
        assertThat(projects).isEmpty();
    }

    @Test
    @DisplayName("Should find all projects where user is owner or member")
    void findAllUserProjects() {
        // Act
        List<Project> ownerProjects = projectRepository.findAllUserProjects(owner.getId());
        List<Project> member1Projects = projectRepository.findAllUserProjects(member1.getId());
        List<Project> member2Projects = projectRepository.findAllUserProjects(member2.getId());

        // Assert
        assertThat(ownerProjects).hasSize(2); // owner owns both projects
        assertThat(member1Projects).hasSize(1); // member1 is only in project1
        assertThat(member1Projects.get(0).getName()).isEqualTo("Project 1");
        assertThat(member2Projects).isEmpty(); // member2 is not in any active project
    }

    @Test
    @DisplayName("Should return distinct projects in findAllUserProjects")
    void findAllUserProjects_ReturnsDistinctProjects() {
        // Act
        List<Project> projects = projectRepository.findAllUserProjects(owner.getId());

        // Assert
        assertThat(projects).hasSize(2);
        assertThat(projects).extracting(Project::getId).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("Should find project by ID when active")
    void findByIdAndIsActiveTrue_Success() {
        // Act
        Optional<Project> found = projectRepository.findByIdAndIsActiveTrue(project1.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Project 1");
    }

    @Test
    @DisplayName("Should not find inactive project by ID")
    void findByIdAndIsActiveTrue_InactiveProject() {
        // Act
        Optional<Project> found = projectRepository.findByIdAndIsActiveTrue(inactiveProject.getId());

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should not find non-existent project by ID")
    void findByIdAndIsActiveTrue_NotFound() {
        // Act
        Optional<Project> found = projectRepository.findByIdAndIsActiveTrue(UUID.randomUUID());

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return true when user is owner of project")
    void isOwner_True() {
        // Act
        boolean isOwner = projectRepository.isOwner(project1.getId(), owner.getId());

        // Assert
        assertThat(isOwner).isTrue();
    }

    @Test
    @DisplayName("Should return false when user is not owner of project")
    void isOwner_False() {
        // Act
        boolean isOwner = projectRepository.isOwner(project1.getId(), member1.getId());

        // Assert
        assertThat(isOwner).isFalse();
    }

    @Test
    @DisplayName("Should return false when user does not exist")
    void isOwner_UserNotExists() {
        // Act
        boolean isOwner = projectRepository.isOwner(project1.getId(), UUID.randomUUID());

        // Assert
        assertThat(isOwner).isFalse();
    }

    @Test
    @DisplayName("Should return false when project does not exist")
    void isOwner_ProjectNotExists() {
        // Act
        boolean isOwner = projectRepository.isOwner(UUID.randomUUID(), owner.getId());

        // Assert
        assertThat(isOwner).isFalse();
    }

    @Test
    @DisplayName("Should return true when user is owner - hasAccess")
    void hasAccess_AsOwner() {
        // Act
        boolean hasAccess = projectRepository.hasAccess(project1.getId(), owner.getId());

        // Assert
        assertThat(hasAccess).isTrue();
    }

    @Test
    @DisplayName("Should return true when user is member - hasAccess")
    void hasAccess_AsMember() {
        // Act
        boolean hasAccess = projectRepository.hasAccess(project1.getId(), member1.getId());

        // Assert
        assertThat(hasAccess).isTrue();
    }

    @Test
    @DisplayName("Should return false when user is not owner or member - hasAccess")
    void hasAccess_NoAccess() {
        // Act
        boolean hasAccess = projectRepository.hasAccess(project1.getId(), member2.getId());

        // Assert
        assertThat(hasAccess).isFalse();
    }

    @Test
    @DisplayName("Should return false when user does not exist - hasAccess")
    void hasAccess_UserNotExists() {
        // Act
        boolean hasAccess = projectRepository.hasAccess(project1.getId(), UUID.randomUUID());

        // Assert
        assertThat(hasAccess).isFalse();
    }

    @Test
    @DisplayName("Should return false when project does not exist - hasAccess")
    void hasAccess_ProjectNotExists() {
        // Act
        boolean hasAccess = projectRepository.hasAccess(UUID.randomUUID(), owner.getId());

        // Assert
        assertThat(hasAccess).isFalse();
    }

    @Test
    @DisplayName("Should return true for inactive project if user has access")
    void hasAccess_InactiveProject() {
        // Arrange
        ProjectMember inactiveMember = ProjectMember.builder()
                .projectId(inactiveProject.getId())
                .userId(member1.getId())
                .role(ProjectRole.MEMBER)
                .joinedAt(LocalDateTime.now())
                .build();
        entityManager.persist(inactiveMember);
        entityManager.flush();

        // Act
        boolean hasAccess = projectRepository.hasAccess(inactiveProject.getId(), member1.getId());

        // Assert
        assertThat(hasAccess).isTrue();
    }

    @Test
    @DisplayName("Should order projects correctly in findAllUserProjects")
    void findAllUserProjects_OrderedByCreatedAtDesc() {
        // Act
        List<Project> projects = projectRepository.findAllUserProjects(owner.getId());

        // Assert
        assertThat(projects).hasSize(2);
        // Should be ordered by created_at DESC (most recent first)
        assertThat(projects.get(0).getCreatedAt()).isAfter(projects.get(1).getCreatedAt());
    }

    @Test
    @DisplayName("Should only return active projects in findAllUserProjects")
    void findAllUserProjects_OnlyActiveProjects() {
        // Arrange - Add owner as member of inactive project
        ProjectMember inactiveMember = ProjectMember.builder()
                .projectId(inactiveProject.getId())
                .userId(owner.getId())
                .role(ProjectRole.OWNER)
                .joinedAt(LocalDateTime.now())
                .build();
        entityManager.persist(inactiveMember);
        entityManager.flush();

        // Act
        List<Project> projects = projectRepository.findAllUserProjects(owner.getId());

        // Assert
        assertThat(projects).hasSize(2); // Should still only return 2 active projects
        assertThat(projects).allMatch(Project::getIsActive);
        assertThat(projects).noneMatch(p -> p.getId().equals(inactiveProject.getId()));
    }
}
