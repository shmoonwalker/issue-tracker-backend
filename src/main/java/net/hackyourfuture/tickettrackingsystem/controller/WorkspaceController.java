package net.hackyourfuture.tickettrackingsystem.controller;


import lombok.RequiredArgsConstructor;
import net.hackyourfuture.tickettrackingsystem.dto.request.CreateWorkspaceRequest;
import net.hackyourfuture.tickettrackingsystem.dto.response.WorkspaceResponse;
import net.hackyourfuture.tickettrackingsystem.service.WorkspaceService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkspaceResponse createWorkspace(@Valid @RequestBody CreateWorkspaceRequest request, Authentication authentication){
        return workspaceService.createWorkspace(request, (Long) authentication.getPrincipal());
    }

    @GetMapping
    public List<WorkspaceResponse> getMyWorkspaces(Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();

        return workspaceService.getMyWorkspaces(currentUserId);
    }



}
