package net.hackyourfuture.tickettrackingsystem.service;

import lombok.RequiredArgsConstructor;
import net.hackyourfuture.tickettrackingsystem.dto.request.CreateProjectRequest;
import net.hackyourfuture.tickettrackingsystem.dto.response.ProjectResponse;

import net.hackyourfuture.tickettrackingsystem.model.Project;
import net.hackyourfuture.tickettrackingsystem.repository.ProjectRepository;
import net.hackyourfuture.tickettrackingsystem.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;

    @Transactional
    public ProjectResponse createProject(Long workspaceId, CreateProjectRequest request, Long currentUserId) {
        if (!workspaceRepository.isMember(workspaceId, currentUserId)) {
            throw new IllegalArgumentException("User is not a member of the workspace");
        }
        Project project = new Project(
                null,
                workspaceId,
                request.name(),
                request.description(),
                currentUserId);

        Project createdProject = projectRepository.create(project);
        return new ProjectResponse(
                createdProject.id(),
                createdProject.workspaceId(),
                createdProject.name(),
                createdProject.description(),
                createdProject.createdByUserId()
        );
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByWorkspace (Long workspaceId, Long currentUserId) {
        if (!workspaceRepository.isMember(workspaceId, currentUserId)) {
            throw new IllegalArgumentException("User is not a member of the workspace");
        }

        return projectRepository.findByWorkspaceId(workspaceId).stream()
                .map(project -> new ProjectResponse(
                        project.id(),
                        project.workspaceId(),
                        project.name(),
                        project.description(),
                        project.createdByUserId()
                ))
                .toList();
    }
}
