package com.amsal.fidmap.changeLog;

import com.amsal.fidmap.apiResponse.ApiResponse;

import java.util.List;
import java.util.UUID;

public interface IChangeLogService {

    ApiResponse<ChangeLogDto> createChangeLog(UUID workspaceId, ChangeLogRequest request);

    ApiResponse<List<ChangeLogDto>> getAllChangeLogs(UUID workspaceId);

    ApiResponse<ChangeLogDto> getChangeLog(UUID changeLogId);

    ApiResponse<ChangeLogDto> editChangeLog(UUID changeLogId,UpdateChangeLogRequest request);

    ApiResponse<ChangeLogDto> editChangeLogStatus(UUID changeLogId,ChangeLogStatus status);

    ApiResponse<ChangeLogDto> deleteChangeLogStatus(UUID changeLogId);
}
