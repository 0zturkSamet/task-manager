package com.taskmanager.service;

import com.taskmanager.dto.UpdateUserRequest;
import com.taskmanager.dto.UserResponse;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .email("john.doe@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .profileImage("https://example.com/avatar.jpg")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should get user profile successfully")
    void getUserProfile_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        UserResponse response = userService.getUserProfile(userId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getProfileImage()).isEqualTo("https://example.com/avatar.jpg");
        assertThat(response.getIsActive()).isTrue();
        assertThat(response.getCreatedAt()).isNotNull();

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw exception when user not found in getUserProfile")
    void getUserProfile_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserProfile(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should map user without profile image correctly")
    void getUserProfile_WithoutProfileImage() {
        // Arrange
        user.setProfileImage(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        UserResponse response = userService.getUserProfile(userId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getProfileImage()).isNull();
    }

    @Test
    @DisplayName("Should update all user profile fields")
    void updateUserProfile_AllFields_Success() {
        // Arrange
        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .profileImage("https://example.com/new-avatar.jpg")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserResponse response = userService.updateUserProfile(userId, request);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User updatedUser = userCaptor.getValue();
        assertThat(updatedUser.getFirstName()).isEqualTo("Jane");
        assertThat(updatedUser.getLastName()).isEqualTo("Smith");
        assertThat(updatedUser.getProfileImage()).isEqualTo("https://example.com/new-avatar.jpg");

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Should update only firstName when other fields are null")
    void updateUserProfile_OnlyFirstName() {
        // Arrange
        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("Jane")
                .build();

        String originalLastName = user.getLastName();
        String originalProfileImage = user.getProfileImage();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        userService.updateUserProfile(userId, request);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User updatedUser = userCaptor.getValue();
        assertThat(updatedUser.getFirstName()).isEqualTo("Jane");
        assertThat(updatedUser.getLastName()).isEqualTo(originalLastName);
        assertThat(updatedUser.getProfileImage()).isEqualTo(originalProfileImage);
    }

    @Test
    @DisplayName("Should update only lastName when other fields are null")
    void updateUserProfile_OnlyLastName() {
        // Arrange
        UpdateUserRequest request = UpdateUserRequest.builder()
                .lastName("Smith")
                .build();

        String originalFirstName = user.getFirstName();
        String originalProfileImage = user.getProfileImage();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        userService.updateUserProfile(userId, request);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User updatedUser = userCaptor.getValue();
        assertThat(updatedUser.getFirstName()).isEqualTo(originalFirstName);
        assertThat(updatedUser.getLastName()).isEqualTo("Smith");
        assertThat(updatedUser.getProfileImage()).isEqualTo(originalProfileImage);
    }

    @Test
    @DisplayName("Should update only profileImage when other fields are null")
    void updateUserProfile_OnlyProfileImage() {
        // Arrange
        UpdateUserRequest request = UpdateUserRequest.builder()
                .profileImage("https://example.com/updated-avatar.jpg")
                .build();

        String originalFirstName = user.getFirstName();
        String originalLastName = user.getLastName();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        userService.updateUserProfile(userId, request);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User updatedUser = userCaptor.getValue();
        assertThat(updatedUser.getFirstName()).isEqualTo(originalFirstName);
        assertThat(updatedUser.getLastName()).isEqualTo(originalLastName);
        assertThat(updatedUser.getProfileImage()).isEqualTo("https://example.com/updated-avatar.jpg");
    }

    @Test
    @DisplayName("Should throw exception when user not found in updateUserProfile")
    void updateUserProfile_UserNotFound_ThrowsException() {
        // Arrange
        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("Jane")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUserProfile(userId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should return updated user in response")
    void updateUserProfile_ReturnsUpdatedUser() {
        // Arrange
        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .build();

        User updatedUser = User.builder()
                .id(userId)
                .email(user.getEmail())
                .password(user.getPassword())
                .firstName("Jane")
                .lastName("Smith")
                .profileImage(user.getProfileImage())
                .isActive(true)
                .createdAt(user.getCreatedAt())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        UserResponse response = userService.updateUserProfile(userId, request);

        // Assert
        assertThat(response.getFirstName()).isEqualTo("Jane");
        assertThat(response.getLastName()).isEqualTo("Smith");
    }

    @Test
    @DisplayName("Should soft delete user account")
    void deleteUserAccount_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        userService.deleteUserAccount(userId);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User deletedUser = userCaptor.getValue();
        assertThat(deletedUser.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when user not found in deleteUserAccount")
    void deleteUserAccount_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUserAccount(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should preserve user data when soft deleting")
    void deleteUserAccount_PreservesUserData() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        userService.deleteUserAccount(userId);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User deletedUser = userCaptor.getValue();
        assertThat(deletedUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(deletedUser.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(deletedUser.getLastName()).isEqualTo(user.getLastName());
        assertThat(deletedUser.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should search users by email")
    void searchUsers_ByEmail_Success() {
        // Arrange
        String searchTerm = "john";
        User user1 = createUser("john.doe@example.com", "John", "Doe");
        User user2 = createUser("john.smith@example.com", "Johnny", "Smith");

        when(userRepository.searchUsers(searchTerm)).thenReturn(Arrays.asList(user1, user2));

        // Act
        List<UserResponse> results = userService.searchUsers(searchTerm);

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getEmail()).isEqualTo("john.doe@example.com");
        assertThat(results.get(1).getEmail()).isEqualTo("john.smith@example.com");

        verify(userRepository).searchUsers(searchTerm);
    }

    @Test
    @DisplayName("Should search users by first name")
    void searchUsers_ByFirstName_Success() {
        // Arrange
        String searchTerm = "Jane";
        User user1 = createUser("jane.doe@example.com", "Jane", "Doe");
        User user2 = createUser("jane.smith@example.com", "Jane", "Smith");

        when(userRepository.searchUsers(searchTerm)).thenReturn(Arrays.asList(user1, user2));

        // Act
        List<UserResponse> results = userService.searchUsers(searchTerm);

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getFirstName()).isEqualTo("Jane");
        assertThat(results.get(1).getFirstName()).isEqualTo("Jane");
    }

    @Test
    @DisplayName("Should search users by last name")
    void searchUsers_ByLastName_Success() {
        // Arrange
        String searchTerm = "Smith";
        User user1 = createUser("john.smith@example.com", "John", "Smith");
        User user2 = createUser("jane.smith@example.com", "Jane", "Smith");

        when(userRepository.searchUsers(searchTerm)).thenReturn(Arrays.asList(user1, user2));

        // Act
        List<UserResponse> results = userService.searchUsers(searchTerm);

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getLastName()).isEqualTo("Smith");
        assertThat(results.get(1).getLastName()).isEqualTo("Smith");
    }

    @Test
    @DisplayName("Should return empty list when search term is null")
    void searchUsers_NullSearchTerm_ReturnsEmptyList() {
        // Act
        List<UserResponse> results = userService.searchUsers(null);

        // Assert
        assertThat(results).isEmpty();
        verify(userRepository, never()).searchUsers(any());
    }

    @Test
    @DisplayName("Should return empty list when search term is empty")
    void searchUsers_EmptySearchTerm_ReturnsEmptyList() {
        // Act
        List<UserResponse> results = userService.searchUsers("");

        // Assert
        assertThat(results).isEmpty();
        verify(userRepository, never()).searchUsers(any());
    }

    @Test
    @DisplayName("Should return empty list when search term is blank")
    void searchUsers_BlankSearchTerm_ReturnsEmptyList() {
        // Act
        List<UserResponse> results = userService.searchUsers("   ");

        // Assert
        assertThat(results).isEmpty();
        verify(userRepository, never()).searchUsers(any());
    }

    @Test
    @DisplayName("Should trim search term before searching")
    void searchUsers_TrimsSearchTerm() {
        // Arrange
        String searchTerm = "  john  ";
        when(userRepository.searchUsers("john")).thenReturn(List.of());

        // Act
        userService.searchUsers(searchTerm);

        // Assert
        verify(userRepository).searchUsers("john");
    }

    @Test
    @DisplayName("Should return empty list when no users match")
    void searchUsers_NoMatches_ReturnsEmptyList() {
        // Arrange
        String searchTerm = "nonexistent";
        when(userRepository.searchUsers(searchTerm)).thenReturn(List.of());

        // Act
        List<UserResponse> results = userService.searchUsers(searchTerm);

        // Assert
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should map all user fields correctly in search results")
    void searchUsers_MapsAllFields() {
        // Arrange
        String searchTerm = "john";
        User searchUser = User.builder()
                .id(UUID.randomUUID())
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .profileImage("https://example.com/avatar.jpg")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.searchUsers(searchTerm)).thenReturn(List.of(searchUser));

        // Act
        List<UserResponse> results = userService.searchUsers(searchTerm);

        // Assert
        assertThat(results).hasSize(1);
        UserResponse response = results.get(0);
        assertThat(response.getId()).isEqualTo(searchUser.getId());
        assertThat(response.getEmail()).isEqualTo(searchUser.getEmail());
        assertThat(response.getFirstName()).isEqualTo(searchUser.getFirstName());
        assertThat(response.getLastName()).isEqualTo(searchUser.getLastName());
        assertThat(response.getProfileImage()).isEqualTo(searchUser.getProfileImage());
        assertThat(response.getIsActive()).isEqualTo(searchUser.getIsActive());
        assertThat(response.getCreatedAt()).isEqualTo(searchUser.getCreatedAt());
    }

    // Helper method
    private User createUser(String email, String firstName, String lastName) {
        return User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .password("encodedPassword")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
