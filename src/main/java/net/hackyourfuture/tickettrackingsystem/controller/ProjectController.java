package net.hackyourfuture.tickettrackingsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.hackyourfuture.tickettrackingsystem.dto.request.CreateProjectRequest;
import net.hackyourfuture.tickettrackingsystem.dto.response.ProjectResponse;
import net.hackyourfuture.tickettrackingsystem.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/workspaces/{workspaceId}/projects")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(
            @PathVariable Long workspaceId,
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        return projectService.createProject(workspaceId, request, currentUserId);
    }

    @GetMapping
    public List<ProjectResponse> getProjectsByWorkspace(@PathVariable Long workspaceId, Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();

        return projectService.getProjectsByWorkspace(workspaceId, currentUserId);
    }
}