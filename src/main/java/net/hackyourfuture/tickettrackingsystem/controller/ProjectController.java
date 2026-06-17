package net.hackyourfuture.tickettrackingsystem.controller;

import lombok.RequiredArgsConstructor;
import net.hackyourfuture.tickettrackingsystem.dto.response.ProjectSummaryResponse;
import net.hackyourfuture.tickettrackingsystem.service.ProjectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public List<ProjectSummaryResponse> getAllProjects() {
        return projectService.getAllProjects();
    }
}