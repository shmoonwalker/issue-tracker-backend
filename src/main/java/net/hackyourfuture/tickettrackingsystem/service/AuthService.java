package net.hackyourfuture.tickettrackingsystem.service;

import lombok.RequiredArgsConstructor;
import net.hackyourfuture.tickettrackingsystem.dto.request.LoginRequest;
import net.hackyourfuture.tickettrackingsystem.dto.request.RegisterRequest;
import net.hackyourfuture.tickettrackingsystem.dto.response.AuthResponse;
import net.hackyourfuture.tickettrackingsystem.dto.response.UserResponse;
import net.hackyourfuture.tickettrackingsystem.model.User;
import net.hackyourfuture.tickettrackingsystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
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

        UserResponse userResponse = new UserResponse(
                createdUser.id(),
                createdUser.name(),
                createdUser.email()
        );

        String token = jwtService.generateToken(createdUser.id(), createdUser.email());

        return new AuthResponse(token, userResponse);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        UserResponse userResponse = new UserResponse(
                user.id(),
                user.name(),
                user.email()
        );

        String token = jwtService.generateToken(user.id(), user.email());

        return new AuthResponse(token, userResponse);
    }
}