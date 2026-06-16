package net.hackyourfuture.tickettrackingsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.hackyourfuture.tickettrackingsystem.dto.request.CreateUserRequest;
import net.hackyourfuture.tickettrackingsystem.dto.request.UpdateUserRequest;
import net.hackyourfuture.tickettrackingsystem.dto.response.UserResponse;
import net.hackyourfuture.tickettrackingsystem.model.User;
import net.hackyourfuture.tickettrackingsystem.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.id(),
                user.name(),
                user.email()
        );
    }
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers()
                .stream()
                .map(this::toUserResponse).
                toList();
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return toUserResponse(
                userService.getUserById(id)
        );
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser( @Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toUserResponse(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        User user = userService.updateUser(id, request);

        return ResponseEntity.ok(toUserResponse(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}
