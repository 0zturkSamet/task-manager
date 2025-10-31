package com.taskmanager.repository;

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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserRepository Integration Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User johnDoe;
    private User janeDoe;
    private User johnSmith;
    private User aliceJohnson;
    private User inactiveUser;

    @BeforeEach
    void setUp() {
        johnDoe = User.builder()
                .email("john.doe@example.com")
                .password("password")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(johnDoe);

        janeDoe = User.builder()
                .email("jane.doe@example.com")
                .password("password")
                .firstName("Jane")
                .lastName("Doe")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(janeDoe);

        johnSmith = User.builder()
                .email("john.smith@example.com")
                .password("password")
                .firstName("John")
                .lastName("Smith")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(johnSmith);

        aliceJohnson = User.builder()
                .email("alice.johnson@example.com")
                .password("password")
                .firstName("Alice")
                .lastName("Johnson")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(aliceJohnson);

        inactiveUser = User.builder()
                .email("inactive@example.com")
                .password("password")
                .firstName("Inactive")
                .lastName("User")
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(inactiveUser);

        entityManager.flush();
    }

    @Test
    @DisplayName("Should find user by email")
    void findByEmail_Success() {
        // Act
        Optional<User> found = userRepository.findByEmail("john.doe@example.com");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("John");
        assertThat(found.get().getLastName()).isEqualTo("Doe");
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    void findByEmail_NotFound() {
        // Act
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check if email exists")
    void existsByEmail_True() {
        // Act
        boolean exists = userRepository.existsByEmail("john.doe@example.com");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void existsByEmail_False() {
        // Act
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should find active user by email")
    void findByEmailAndIsActiveTrue_Success() {
        // Act
        Optional<User> found = userRepository.findByEmailAndIsActiveTrue("john.doe@example.com");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should not find inactive user by email")
    void findByEmailAndIsActiveTrue_InactiveUser() {
        // Act
        Optional<User> found = userRepository.findByEmailAndIsActiveTrue("inactive@example.com");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should search users by email")
    void searchUsers_ByEmail() {
        // Act
        List<User> results = userRepository.searchUsers("john.doe");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("Should search users by first name")
    void searchUsers_ByFirstName() {
        // Act
        List<User> results = userRepository.searchUsers("John");

        // Assert
        assertThat(results).hasSize(2); // johnDoe and johnSmith
        assertThat(results).extracting(User::getFirstName)
                .allMatch(name -> name.equals("John"));
    }

    @Test
    @DisplayName("Should search users by last name")
    void searchUsers_ByLastName() {
        // Act
        List<User> results = userRepository.searchUsers("Doe");

        // Assert
        assertThat(results).hasSize(2); // johnDoe and janeDoe
        assertThat(results).extracting(User::getLastName)
                .allMatch(name -> name.equals("Doe"));
    }

    @Test
    @DisplayName("Should search users case-insensitively")
    void searchUsers_CaseInsensitive() {
        // Act
        List<User> resultsLower = userRepository.searchUsers("john");
        List<User> resultsUpper = userRepository.searchUsers("JOHN");
        List<User> resultsMixed = userRepository.searchUsers("JoHn");

        // Assert
        assertThat(resultsLower).hasSize(2);
        assertThat(resultsUpper).hasSize(2);
        assertThat(resultsMixed).hasSize(2);
        assertThat(resultsLower).containsExactlyInAnyOrderElementsOf(resultsUpper);
    }

    @Test
    @DisplayName("Should search users with partial match")
    void searchUsers_PartialMatch() {
        // Act
        List<User> results = userRepository.searchUsers("jo");

        // Assert
        assertThat(results).hasSizeGreaterThanOrEqualTo(3); // johnDoe, johnSmith, aliceJohnson
    }

    @Test
    @DisplayName("Should only return active users in search")
    void searchUsers_OnlyActiveUsers() {
        // Act
        List<User> results = userRepository.searchUsers("User");

        // Assert
        assertThat(results).isEmpty(); // inactiveUser should not be returned
        assertThat(results).noneMatch(user -> !user.getIsActive());
    }

    @Test
    @DisplayName("Should return empty list when no users match search")
    void searchUsers_NoMatches() {
        // Act
        List<User> results = userRepository.searchUsers("NonexistentName");

        // Assert
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should order search results by first name then last name")
    void searchUsers_OrderedByName() {
        // Act
        List<User> results = userRepository.searchUsers("o"); // Matches multiple users

        // Assert
        assertThat(results).isNotEmpty();

        // Verify ordering: firstName ASC, lastName ASC
        for (int i = 0; i < results.size() - 1; i++) {
            User current = results.get(i);
            User next = results.get(i + 1);

            int firstNameComparison = current.getFirstName().compareTo(next.getFirstName());
            if (firstNameComparison == 0) {
                // If first names are equal, last names should be ordered
                assertThat(current.getLastName()).isLessThanOrEqualTo(next.getLastName());
            } else {
                // First names should be ordered
                assertThat(firstNameComparison).isLessThanOrEqualTo(0);
            }
        }
    }

    @Test
    @DisplayName("Should search by email domain")
    void searchUsers_ByEmailDomain() {
        // Act
        List<User> results = userRepository.searchUsers("example.com");

        // Assert
        assertThat(results).hasSize(4); // All active users have example.com
    }

    @Test
    @DisplayName("Should find user by exact email match in search")
    void searchUsers_ExactEmailMatch() {
        // Act
        List<User> results = userRepository.searchUsers("alice.johnson@example.com");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEmail()).isEqualTo("alice.johnson@example.com");
    }

    @Test
    @DisplayName("Should search users with special characters in search term")
    void searchUsers_WithSpecialCharacters() {
        // Act
        List<User> results = userRepository.searchUsers("john.doe@");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEmail()).contains("john.doe@");
    }

    @Test
    @DisplayName("Should handle empty string search")
    void searchUsers_EmptyString() {
        // Act
        List<User> results = userRepository.searchUsers("");

        // Assert
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should search across all searchable fields")
    void searchUsers_MultipleFieldMatch() {
        // Arrange - Create a user with "test" in multiple fields
        User multiFieldUser = User.builder()
                .email("test@example.com")
                .password("password")
                .firstName("Test")
                .lastName("Tester")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(multiFieldUser);
        entityManager.flush();

        // Act
        List<User> results = userRepository.searchUsers("test");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEmail()).isEqualTo("test@example.com");
    }
}
