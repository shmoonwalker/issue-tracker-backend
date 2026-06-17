package net.hackyourfuture.tickettrackingsystem.service;

import lombok.RequiredArgsConstructor;
import net.hackyourfuture.tickettrackingsystem.dto.response.ProjectSummaryResponse;
import net.hackyourfuture.tickettrackingsystem.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public List<ProjectSummaryResponse> getAllProjects() {
        return projectRepository.findAllProjectSummaries();
    }
}
