package com.amsal.fidmap.changeLog;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateChangeLogRequest {

    private String title;

    private String content;

    private ChangeLogStatus status;

    private LocalDateTime publishedAt;
}
