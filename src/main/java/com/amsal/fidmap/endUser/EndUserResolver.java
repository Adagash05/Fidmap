package com.amsal.fidmap.endUser;

import com.amsal.fidmap.payment.billing.PlanEntitlementService;
import com.amsal.fidmap.workspace.Workspace;
import com.amsal.fidmap.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Single place that turns public "name + email" input into an EndUser that is
 * correctly linked to the workspace the interaction happened in.
 *
 * Fixes: the previous code set the inverse side of the many-to-many, so no
 * link was ever persisted and the end-user plan limit never counted anyone.
 *
 * NOTE: end users are identified by an UNVERIFIED email address. That is an
 * accepted, documented limitation (see SECURITY_AND_TENANCY_AUDIT.md).
 */
@Component
@RequiredArgsConstructor
public class EndUserResolver {

    private static final Pattern EMAIL =
            Pattern.compile("^[^\\s@]{1,64}@[^\\s@]{1,190}\\.[^\\s@]{2,}$");

    private final EndUserRepository endUserRepository;
    private final WorkspaceRepository workspaceRepository;
    private final PlanEntitlementService planEntitlementService;

    public static String normalizeEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("A valid email address is required");
        }

        String normalized = email.trim().toLowerCase();

        if (normalized.length() > 254 || !EMAIL.matcher(normalized).matches()) {
            throw new IllegalArgumentException("A valid email address is required");
        }

        return normalized;
    }

    public static String cleanName(String name, String normalizedEmail) {

        String value = name == null ? "" : name.trim();

        if (value.isEmpty()) {
            value = normalizedEmail.substring(0, normalizedEmail.indexOf('@'));
        }

        return value.length() > 120 ? value.substring(0, 120) : value;
    }

    @Transactional
    public EndUser resolve(Workspace workspace, String name, String email) {

        String normalized = normalizeEmail(email);

        List<EndUser> matches = endUserRepository.findAllByEmailIgnoreCase(normalized);

        EndUser endUser;

        if (matches.isEmpty()) {

            endUser = new EndUser();
            endUser.setEmail(normalized);
            endUser.setName(cleanName(name, normalized));

        } else {
            endUser = matches.get(0);
        }

        boolean alreadyLinked = endUser.getId() != null
                && endUserRepository.isLinkedToWorkspace(endUser.getId(), workspace.getId());

        if (!alreadyLinked) {

            long current = endUserRepository.countByWorkspaceId(workspace.getId());
            planEntitlementService.checkEndUserLimit(workspace.getId(), current);
        }

        if (endUser.getId() == null) {
            endUser = endUserRepository.save(endUser);
        }

        if (!alreadyLinked) {
            workspaceRepository.linkEndUser(workspace.getId(), endUser.getId());
        }

        return endUser;
    }
}
