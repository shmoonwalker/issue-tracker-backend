package net.hackyourfuture.tickettrackingsystem.service;

import lombok.RequiredArgsConstructor;
import net.hackyourfuture.tickettrackingsystem.dto.response.ProjectSummaryResponse;
import net.hackyourfuture.tickettrackingsystem.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    public List<ProjectSummaryResponse> getAllProjects() {
        return projectRepository.findAllProjectSummaries();
    }
}