package net.hackyourfuture.tickettrackingsystem.service;


import lombok.RequiredArgsConstructor;
import net.hackyourfuture.tickettrackingsystem.dto.request.CreateWorkspaceRequest;
import net.hackyourfuture.tickettrackingsystem.dto.response.WorkspaceResponse;
import net.hackyourfuture.tickettrackingsystem.model.MemberRole;
import net.hackyourfuture.tickettrackingsystem.model.Workspace;
import net.hackyourfuture.tickettrackingsystem.model.WorkspaceMember;
import net.hackyourfuture.tickettrackingsystem.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;


    @Transactional
    public WorkspaceResponse createWorkspace(CreateWorkspaceRequest request,Long currentUserId){
        Workspace workspace = new Workspace(
                null,
                request.name(),
                currentUserId);

        Workspace createdWorkspace = workspaceRepository.create(workspace);
        WorkspaceMember ownerMembership = new WorkspaceMember(
                createdWorkspace.id(),
                currentUserId,
                MemberRole.OWNER,
                null
        );
        workspaceRepository.addMember(ownerMembership);

        return new WorkspaceResponse(createdWorkspace.id(),
                createdWorkspace.name(),
                createdWorkspace.createdByUserId()
        );

    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getMyWorkspaces(Long currentUserId){
        return workspaceRepository.findByUserId(currentUserId)
                .stream()
                .map(workspace -> new WorkspaceResponse(workspace.id(), workspace.name(), workspace.createdByUserId()))
                .collect(Collectors.toList());
    }
}
