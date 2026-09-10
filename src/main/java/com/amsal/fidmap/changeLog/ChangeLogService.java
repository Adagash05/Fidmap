package com.amsal.fidmap.changeLog;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.exception.ChangeLogNotFoundException;
import com.amsal.fidmap.exception.UserNotFoundException;
import com.amsal.fidmap.exception.WorkspaceNotFoundException;
import com.amsal.fidmap.payment.billing.PlanEntitlementService;
import com.amsal.fidmap.security.SecurityUtils;
import com.amsal.fidmap.user.User;
import com.amsal.fidmap.user.UserRepository;
import com.amsal.fidmap.workspace.Workspace;
import com.amsal.fidmap.workspace.WorkspaceRepository;
import com.amsal.fidmap.workspace.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChangeLogService implements IChangeLogService {

    private final ChangeLogRepository changeLogRepository;
    private final UserRepository userRepository;
    private final ChangeLogMapper changeLogMapper;
    private final WorkspaceRepository workspaceRepository;
    private final PlanEntitlementService planEntitlementService;


    @Override
    public ApiResponse<ChangeLogDto> createChangeLog(UUID workspaceId, ChangeLogRequest request) {

        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("user not found,sorry you are not allowed to access this resource, please try again later, thank you");
        }

        //membership != null &&
//        WorkspaceMembership membership = workspaceMemberShipService.getMembership(userId, workspaceId);
        if (workspaceRepository.existsByUser(user)) {

            Workspace workspace = workspaceRepository.findWorkspaceById(workspaceId);
            if (workspace == null) {
                throw new WorkspaceNotFoundException("workspace not found");
            }

            //Plan check
            long currentLogs = changeLogRepository.countByWorkspaceId(workspaceId);
            planEntitlementService.checkChangelogEntryLimit(workspaceId,currentLogs);

            ChangeLog changeLog = changeLogMapper.toChangeLog(request);
            changeLog.setUser(user);
            changeLog.setWorkspace(workspace);

            changeLogRepository.save(changeLog);

            ChangeLogDto dto = changeLogMapper.toChangeLogDto(changeLog);

            return ApiResponse.success("change log is created successfully", dto);


        } else {
            throw new WorkspaceNotFoundException("you are not allowed to access any resource from this workspace");
        }


    }

    @Override
    public ApiResponse<List<ChangeLogDto>> getAllChangeLogs(UUID workspaceId) {

        List<ChangeLogDto> dtos = changeLogRepository.findAllChangeLogByWorkspace(workspaceId)
                .stream()
                .map(changeLogMapper::toChangeLogDto)
                .toList();


        return ApiResponse.success("retrieve all change logs", dtos);
    }

    @Override
    public ApiResponse<ChangeLogDto> getChangeLog(UUID changeLogId) {

        ChangeLog log = changeLogRepository.findChangeLogById(changeLogId);

        ChangeLogDto dto = changeLogMapper.toChangeLogDto(log);

        return ApiResponse.success("retrieve change log", dto);

    }

    @Transactional
    @Override
    public ApiResponse<ChangeLogDto> editChangeLog(UUID changeLogId, UpdateChangeLogRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("user not found,sorry you are not allowed to access this resource, please try again later, thank you");
        }

        ChangeLog changeLog = changeLogRepository.findChangeLogById(changeLogId);
        if (changeLog == null) {
            throw new ChangeLogNotFoundException("change log is not found");
        }

        var changeLogWorkspace = changeLog.getWorkspace();

        if (!user.getWorkspace().equals(changeLogWorkspace)) {

            throw new WorkspaceNotFoundException("sorry, you are not allowed to access this workspace resources");


        } else {


            changeLog.setTitle(request.getTitle());
            changeLog.setContent(request.getContent());
            changeLog.setPublishedAt(request.getPublishedAt());
            changeLog.setStatus(request.getStatus());

            changeLogRepository.save(changeLog);

            ChangeLogDto dto = changeLogMapper.toChangeLogDto(changeLog);

            return ApiResponse.success("change log status is updated successfully", dto);

        }
    }


    @Override
    public ApiResponse<ChangeLogDto> editChangeLogStatus(UUID changeLogId, ChangeLogStatus status) {

        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("user not found,sorry you are not allowed to access this resource, please try again later, thank you");
        }

        ChangeLog changeLog = changeLogRepository.findChangeLogById(changeLogId);
        if (changeLog == null) {
            throw new ChangeLogNotFoundException("change log is not found");
        }

        var changeLogWorkspace = changeLog.getWorkspace();

        if (!user.getWorkspace().equals(changeLogWorkspace)) {

            throw new WorkspaceNotFoundException("sorry, you are not allowed to access this workspace resources");


        } else {

            changeLog.setStatus(status);

            changeLogRepository.save(changeLog);

            ChangeLogDto dto = changeLogMapper.toChangeLogDto(changeLog);

            return ApiResponse.success("change log status is updated successfully", dto);
        }
    }

    @Override
    public ApiResponse<ChangeLogDto> deleteChangeLogStatus(UUID changeLogId) {

        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("user not found,sorry you are not allowed to access this resource, please try again later, thank you");
        }

        ChangeLog changeLog = changeLogRepository.findChangeLogById(changeLogId);
        if (changeLog == null) {
            throw new ChangeLogNotFoundException("change log is not found");
        }

        changeLogRepository.delete(changeLog);

        return ApiResponse.success("this change log is deleted successfully", null);
    }
}
