package com.amsal.fidmap.changeLog;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ChangeLogDto {

    private UUID id;

    private String title;

    private String content;

    private ChangeLogStatus status;

    private LocalDateTime publishedAt;

    private String authorName;

}
