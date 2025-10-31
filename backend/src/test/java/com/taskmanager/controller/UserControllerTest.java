package com.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.dto.UpdateUserRequest;
import com.taskmanager.dto.UserResponse;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.security.JwtAuthenticationFilter;
import com.taskmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = UserController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
@DisplayName("UserController Integration Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User authenticatedUser;
    private UUID userId;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        authenticatedUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        userResponse = UserResponse.builder()
                .id(userId)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .profileImage("https://example.com/avatar.jpg")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("GET /api/users/profile - Should get user profile successfully")
    @WithMockUser
    void getUserProfile_Success() throws Exception {
        // Arrange
        when(userService.getUserProfile(userId)).thenReturn(userResponse);

        // Act & Assert
        mockMvc.perform(get("/api/users/profile")
                .with(user(authenticatedUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.profileImage").value("https://example.com/avatar.jpg"));

        verify(userService).getUserProfile(userId);
    }

    @Test
    @DisplayName("GET /api/users/profile - Should return 401 when not authenticated")
    void getUserProfile_NotAuthenticated_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getUserProfile(any());
    }

    @Test
    @DisplayName("GET /api/users/profile - Should return 404 when user not found")
    @WithMockUser
    void getUserProfile_UserNotFound_NotFound() throws Exception {
        // Arrange
        when(userService.getUserProfile(userId))
                .thenThrow(new ResourceNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/api/users/profile")
                .with(user(authenticatedUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/users/profile - Should update user profile successfully")
    @WithMockUser
    void updateUserProfile_Success() throws Exception {
        // Arrange
        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .profileImage("https://example.com/new-avatar.jpg")
                .build();

        UserResponse updatedResponse = UserResponse.builder()
                .id(userId)
                .email("test@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .profileImage("https://example.com/new-avatar.jpg")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.updateUserProfile(eq(userId), any(UpdateUserRequest.class)))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/users/profile")
                .with(user(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.profileImage").value("https://example.com/new-avatar.jpg"));

        verify(userService).updateUserProfile(eq(userId), any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("PUT /api/users/profile - Should update only firstName")
    @WithMockUser
    void updateUserProfile_OnlyFirstName() throws Exception {
        // Arrange
        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("Jane")
                .build();

        UserResponse updatedResponse = UserResponse.builder()
                .id(userId)
                .email("test@example.com")
                .firstName("Jane")
                .lastName("Doe")
                .profileImage("https://example.com/avatar.jpg")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.updateUserProfile(eq(userId), any(UpdateUserRequest.class)))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/users/profile")
                .with(user(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"));

        verify(userService).updateUserProfile(eq(userId), any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("PUT /api/users/profile - Should return 401 when not authenticated")
    void updateUserProfile_NotAuthenticated_Unauthorized() throws Exception {
        // Arrange
        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("Jane")
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).updateUserProfile(any(), any());
    }

    @Test
    @DisplayName("PUT /api/users/profile - Should return 404 when user not found")
    @WithMockUser
    void updateUserProfile_UserNotFound_NotFound() throws Exception {
        // Arrange
        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("Jane")
                .build();

        when(userService.updateUserProfile(eq(userId), any(UpdateUserRequest.class)))
                .thenThrow(new ResourceNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(put("/api/users/profile")
                .with(user(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/users/account - Should delete user account successfully")
    @WithMockUser
    void deleteUserAccount_Success() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUserAccount(userId);

        // Act & Assert
        mockMvc.perform(delete("/api/users/account")
                .with(user(authenticatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account deleted successfully"));

        verify(userService).deleteUserAccount(userId);
    }

    @Test
    @DisplayName("DELETE /api/users/account - Should return 401 when not authenticated")
    void deleteUserAccount_NotAuthenticated_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/users/account"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).deleteUserAccount(any());
    }

    @Test
    @DisplayName("DELETE /api/users/account - Should return 404 when user not found")
    @WithMockUser
    void deleteUserAccount_UserNotFound_NotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("User not found"))
                .when(userService).deleteUserAccount(userId);

        // Act & Assert
        mockMvc.perform(delete("/api/users/account")
                .with(user(authenticatedUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/users/search - Should search users successfully")
    @WithMockUser
    void searchUsers_Success() throws Exception {
        // Arrange
        UserResponse user1 = UserResponse.builder()
                .id(UUID.randomUUID())
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        UserResponse user2 = UserResponse.builder()
                .id(UUID.randomUUID())
                .email("john.smith@example.com")
                .firstName("John")
                .lastName("Smith")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        List<UserResponse> users = Arrays.asList(user1, user2);

        when(userService.searchUsers("john")).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/users/search")
                .param("q", "john")
                .with(user(authenticatedUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].firstName").value("John"));

        verify(userService).searchUsers("john");
    }

    @Test
    @DisplayName("GET /api/users/search - Should return empty list when no matches")
    @WithMockUser
    void searchUsers_NoMatches() throws Exception {
        // Arrange
        when(userService.searchUsers("nonexistent")).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/users/search")
                .param("q", "nonexistent")
                .with(user(authenticatedUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService).searchUsers("nonexistent");
    }

    @Test
    @DisplayName("GET /api/users/search - Should return 401 when not authenticated")
    void searchUsers_NotAuthenticated_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users/search")
                .param("q", "john"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).searchUsers(any());
    }

    @Test
    @DisplayName("GET /api/users/search - Should return 400 when search parameter is missing")
    @WithMockUser
    void searchUsers_MissingParameter_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users/search")
                .with(user(authenticatedUser)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).searchUsers(any());
    }

    @Test
    @DisplayName("GET /api/users/search - Should search with empty query")
    @WithMockUser
    void searchUsers_EmptyQuery() throws Exception {
        // Arrange
        when(userService.searchUsers("")).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/users/search")
                .param("q", "")
                .with(user(authenticatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService).searchUsers("");
    }
}
