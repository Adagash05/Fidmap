package com.amsal.fidmap.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository repository;

    /** Runs in its own transaction so an audit row survives a rolled-back business action only if it committed. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID workspaceId, UUID actorId, String action, String targetType, String targetId, String detail) {

        repository.save(AuditLog.builder()
                .workspaceId(workspaceId)
                .actorId(actorId)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .detail(detail == null ? null : detail.length() > 500 ? detail.substring(0, 500) : detail)
                .createdAt(Instant.now())
                .build());
    }
}
