package net.hackyourfuture.tickettrackingsystem.service;


import lombok.RequiredArgsConstructor;
import net.hackyourfuture.tickettrackingsystem.dto.request.CreateUserRequest;
import net.hackyourfuture.tickettrackingsystem.dto.request.UpdateUserRequest;
import net.hackyourfuture.tickettrackingsystem.exception.DuplicateEmailException;
import net.hackyourfuture.tickettrackingsystem.model.User;
import net.hackyourfuture.tickettrackingsystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import net.hackyourfuture.tickettrackingsystem.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(CreateUserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new DuplicateEmailException(
                    "Email " + request.email() + " already exists");
        }
        return userRepository.create(new User(
                null,
                request.name(),
                request.email()
        ));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                       new ResourceNotFoundException(
                                "User with id " + id + " not found"));
    }

    public void deleteUser(Long id) {
        boolean deleted = userRepository.deleteById(id);

        if (!deleted) {
            throw new ResourceNotFoundException(
                    "User with id " + id + " not found");
        }
    }

    public User updateUser(Long id, UpdateUserRequest request) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User with id " + id + " not found"));

        Optional<User> userWithSameEmail =
                userRepository.findByEmail(request.email());

        if (userWithSameEmail.isPresent()
                && !userWithSameEmail.get().id().equals(id)) {
            throw new DuplicateEmailException(
                    "Email " + request.email() + " already exists");
        }

        User updatedUser = new User(
                existingUser.id(),
                request.name(),
                request.email()
        );

        userRepository.update(updatedUser);

        return updatedUser;
    }
}
