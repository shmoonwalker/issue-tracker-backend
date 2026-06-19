package net.hackyourfuture.tickettrackingsystem.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.hackyourfuture.tickettrackingsystem.dto.request.RegisterRequest;
import net.hackyourfuture.tickettrackingsystem.dto.response.UserResponse;
import net.hackyourfuture.tickettrackingsystem.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}
