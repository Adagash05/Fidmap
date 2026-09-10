package com.amsal.fidmap.changeLog;

import org.springframework.stereotype.Service;

@Service
public class ChangeLogMapper {

    ChangeLog toChangeLog(ChangeLogRequest request) {

        ChangeLog log = new ChangeLog();
        log.setTitle(request.getTitle());
        log.setContent(request.getContent());
        log.setPublishedAt(request.getPublishedAt());
        log.setStatus(request.getStatus() != null ? request.getStatus() : ChangeLogStatus.PUBLISHED);

        return log;
    }

    public ChangeLogDto toChangeLogDto(ChangeLog changeLog) {

        ChangeLogDto dto = new ChangeLogDto();
        dto.setId(changeLog.getId());
        dto.setTitle(changeLog.getTitle());
        dto.setContent(changeLog.getContent());
        dto.setPublishedAt(changeLog.getPublishedAt());
        dto.setAuthorName(changeLog.getUser().getFullName());

        return dto;
    }
}
