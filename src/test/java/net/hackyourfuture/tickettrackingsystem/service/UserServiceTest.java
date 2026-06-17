package net.hackyourfuture.tickettrackingsystem.service;

import net.hackyourfuture.tickettrackingsystem.dto.request.CreateUserRequest;
import net.hackyourfuture.tickettrackingsystem.dto.request.UpdateUserRequest;
import net.hackyourfuture.tickettrackingsystem.dto.response.UserResponse;
import net.hackyourfuture.tickettrackingsystem.exception.DuplicateEmailException;
import net.hackyourfuture.tickettrackingsystem.exception.ResourceNotFoundException;
import net.hackyourfuture.tickettrackingsystem.model.User;
import net.hackyourfuture.tickettrackingsystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_whenEmailAlreadyExists_throwsDuplicateEmailException() {
        CreateUserRequest request = new CreateUserRequest(
                "Alice Doe",
                "alice@example.com"
        );

        when(userRepository.findByEmail("alice@example.com"))
                .thenReturn(Optional.of(new User(1L, "Alice Doe", "alice@example.com")));

        assertThrows(
                DuplicateEmailException.class,
                () -> userService.createUser(request)
        );

        verify(userRepository, never()).create(any());
    }

    @Test
    void createUser_whenEmailIsFree_persistsAndReturnsResponse() {
        CreateUserRequest request = new CreateUserRequest(
                "Carol",
                "carol@example.com"
        );

        when(userRepository.findByEmail("carol@example.com"))
                .thenReturn(Optional.empty());
        when(userRepository.create(any(User.class)))
                .thenReturn(new User(7L, "Carol", "carol@example.com"));

        UserResponse response = userService.createUser(request);

        assertEquals(7L, response.id());
        assertEquals("Carol", response.name());
        assertEquals("carol@example.com", response.email());
    }

    @Test
    void updateUser_whenEmailIsTakenByAnotherUser_throwsDuplicateEmailException() {
        UpdateUserRequest request = new UpdateUserRequest(
                "Alice Renamed",
                "bob@example.com"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(new User(1L, "Alice Doe", "alice@example.com")));
        when(userRepository.findByEmail("bob@example.com"))
                .thenReturn(Optional.of(new User(2L, "Bob Smith", "bob@example.com")));

        assertThrows(
                DuplicateEmailException.class,
                () -> userService.updateUser(1L, request)
        );

        verify(userRepository, never()).update(any());
    }

    @Test
    void updateUser_whenEmailUnchanged_keepsGoingAndReturnsUpdatedUser() {
        UpdateUserRequest request = new UpdateUserRequest(
                "Alice Renamed",
                "alice@example.com"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(new User(1L, "Alice Doe", "alice@example.com")));
        when(userRepository.findByEmail("alice@example.com"))
                .thenReturn(Optional.of(new User(1L, "Alice Doe", "alice@example.com")));
        when(userRepository.update(any(User.class)))
                .thenReturn(new User(1L, "Alice Renamed", "alice@example.com"));

        UserResponse response = userService.updateUser(1L, request);

        assertEquals("Alice Renamed", response.name());
    }

    @Test
    void deleteUser_whenUserMissing_throwsResourceNotFoundException() {
        when(userRepository.deleteById(99L)).thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.deleteUser(99L)
        );
    }
}
