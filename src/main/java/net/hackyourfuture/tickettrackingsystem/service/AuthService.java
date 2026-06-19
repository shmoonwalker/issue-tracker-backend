package net.hackyourfuture.tickettrackingsystem.service;
import lombok.AllArgsConstructor;
import net.hackyourfuture.tickettrackingsystem.dto.request.RegisterRequest;
import net.hackyourfuture.tickettrackingsystem.dto.response.UserResponse;
import net.hackyourfuture.tickettrackingsystem.model.User;
import net.hackyourfuture.tickettrackingsystem.repository.UserRepository;
import net.hackyourfuture.tickettrackingsystem.config.SecurityConfig;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already in use");
        }
        String passwordHash = passwordEncoder.encode(request.password());

        User userToCreate = new User(
                null,
                request.name(),
                request.email(),
                passwordHash
        );

        User createdUser = userRepository.create(userToCreate);

        return new UserResponse(
                createdUser.id(),
                createdUser.name(),
                createdUser.email()
        );
    }
}
