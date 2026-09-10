package com.amsal.fidmap.changeLog;

import com.amsal.fidmap.apiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workspaces")
public class ChangeLogController {

    private final ChangeLogService changeLogService;

    @PostMapping("/{workspace-id}")
    public ResponseEntity<ApiResponse<ChangeLogDto>> createChangeLog(
            @PathVariable("workspace-id") UUID workspaceId,
            @RequestBody ChangeLogRequest request
    ) {
      return  ResponseEntity
                .status(CREATED)
                .body(changeLogService.createChangeLog(workspaceId, request));

    }

    @GetMapping("/{workspace-id}/changelogs")
    public ResponseEntity<ApiResponse<List<ChangeLogDto>>> getAllChangeLogs(
            @PathVariable("workspace-id") UUID workspaceId
    ) {

        return  ResponseEntity
                .status(CREATED)
                .body(changeLogService.getAllChangeLogs(workspaceId));
    }

    @GetMapping("/changelog/{changelog-id}")
    public ResponseEntity<ApiResponse<ChangeLogDto>> getChangeLogs(
            @PathVariable("changelog-id") UUID changeLogId
    ) {

        return ResponseEntity
                .status(CREATED)
                .body(changeLogService.getChangeLog(changeLogId));
    }

    @PutMapping("/changelog/{id}")
    public ResponseEntity<ApiResponse<ChangeLogDto>> editChangeLog(
            @PathVariable("id") UUID changeLogId,
            @RequestBody UpdateChangeLogRequest request) {
        return ResponseEntity
                .status(ACCEPTED)
                .body(changeLogService.editChangeLog(changeLogId, request));
    }

    @PatchMapping("/changelog/{changelog-id}")
    public ResponseEntity<ApiResponse<ChangeLogDto>> editChangeLogStatus(
            @PathVariable("changelog-id") UUID changeLogId,
            @RequestParam("status") ChangeLogStatus status) {

        return ResponseEntity
                .status(ACCEPTED)
                .body(changeLogService.editChangeLogStatus(changeLogId, status));
    }

    @DeleteMapping("/{changelog-id}")
    public ResponseEntity<ApiResponse<ChangeLogDto>> deleteChangeLogStatus(
            @PathVariable("changelog-id") UUID changeLogId
    ) {
        return ResponseEntity
                .status(NO_CONTENT)
                .body(changeLogService.deleteChangeLogStatus(changeLogId));
    }


}
