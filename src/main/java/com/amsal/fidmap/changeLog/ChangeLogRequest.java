package com.amsal.fidmap.changeLog;

import com.amsal.fidmap.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ChangeLogRequest {

    private String title;

    private String content;

    private ChangeLogStatus status;

    private LocalDateTime publishedAt;

    private UUID userId;
}
