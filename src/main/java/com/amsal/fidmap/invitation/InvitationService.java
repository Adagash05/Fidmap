package com.amsal.fidmap.invitation;

import com.amsal.fidmap.audit.AuditService;
import com.amsal.fidmap.config.FidmapConfig;
import com.amsal.fidmap.exception.UserAlreadyExistsException;
import com.amsal.fidmap.mail.EmailDeliveryException;
import com.amsal.fidmap.mail.EmailService;
import com.amsal.fidmap.payment.billing.PlanEntitlementService;
import com.amsal.fidmap.security.WorkspaceAuthorizationService;
import com.amsal.fidmap.user.Role;
import com.amsal.fidmap.user.User;
import com.amsal.fidmap.user.UserRepository;
import com.amsal.fidmap.workspace.Workspace;
import com.amsal.fidmap.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private static final Logger log = LoggerFactory.getLogger(InvitationService.class);
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]{1,64}@[^\\s@]{1,190}\\.[^\\s@]{2,}$");
    private static final SecureRandom RANDOM = new SecureRandom();
    static final Duration TTL = Duration.ofDays(7);

    private final TeamInvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final PlanEntitlementService planEntitlementService;
    private final WorkspaceAuthorizationService authorization;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final FidmapConfig fidmapConfig;
    private final AuditService auditService;

    public record InviteRequest(String email, Role role) {}

    public record AcceptRequest(String token, String fullName, String password) {}

    public record InvitationView(UUID id, String email, Role role, InvitationStatus status,
                                 Instant createdAt, Instant expiresAt, boolean expired, boolean emailSent) {}

    public record Preview(String workspaceName, String email, Role role) {}

    // ----- manager actions --------------------------------------------------------------------

    @Transactional
    public InvitationView invite(UUID workspaceId, InviteRequest request) {

        User inviter = authorization.requireManagerWrite(workspaceId);

        String email = normalizeEmail(request == null ? null : request.email());
        Role role = request.role() == null ? Role.MEMBER : request.role();

        if (role != Role.MEMBER && role != Role.ADMIN) {
            throw new IllegalArgumentException("You can invite members or admins");
        }

        if (role == Role.ADMIN && inviter.getRole() != Role.OWNER) {
            throw new AccessDeniedException("Only the workspace owner can invite an admin");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new UserAlreadyExistsException("A user with this email already exists");
        }

        requireSeatAvailable(workspaceId);

        // A new invite for the same address replaces any older pending one.
        for (TeamInvitation old : invitationRepository
                .findByWorkspaceIdAndEmailIgnoreCaseAndStatus(workspaceId, email, InvitationStatus.PENDING)) {
            old.setStatus(InvitationStatus.REVOKED);
            invitationRepository.save(old);
        }

        String token = newToken();

        TeamInvitation invitation = invitationRepository.save(TeamInvitation.builder()
                .workspaceId(workspaceId)
                .email(email)
                .role(role)
                .tokenHash(hash(token))
                .status(InvitationStatus.PENDING)
                .invitedBy(inviter.getId())
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(TTL))
                .build());

        boolean sent = sendEmail(invitation, token);

        auditService.record(workspaceId, inviter.getId(), "INVITATION_CREATED", "invitation",
                invitation.getId().toString(), role.name());

        return view(invitation, sent);
    }

    @Transactional(readOnly = true)
    public List<InvitationView> list(UUID workspaceId) {

        authorization.requireManager(workspaceId);

        return invitationRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId).stream()
                .map(i -> view(i, i.getLastEmailError() == null))
                .toList();
    }

    /** Issues a fresh token (the old one stops working) and re-sends the email. */
    @Transactional
    public InvitationView resend(UUID invitationId) {

        TeamInvitation invitation = load(invitationId);

        authorization.requireManagerWrite(invitation.getWorkspaceId());

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Only pending invitations can be re-sent");
        }

        String token = newToken();
        invitation.setTokenHash(hash(token));
        invitation.setExpiresAt(Instant.now().plus(TTL));
        invitationRepository.save(invitation);

        return view(invitation, sendEmail(invitation, token));
    }

    @Transactional
    public void revoke(UUID invitationId) {

        TeamInvitation invitation = load(invitationId);

        authorization.requireManager(invitation.getWorkspaceId());

        if (invitation.getStatus() == InvitationStatus.PENDING) {
            invitation.setStatus(InvitationStatus.REVOKED);
            invitationRepository.save(invitation);
        }
    }

    // ----- public (token holder) --------------------------------------------------------------

    @Transactional(readOnly = true)
    public Preview preview(String token) {

        TeamInvitation invitation = requireUsable(token);

        Workspace workspace = workspaceRepository.findWorkspaceById(invitation.getWorkspaceId());

        return new Preview(workspace == null ? "" : workspace.getName(), invitation.getEmail(), invitation.getRole());
    }

    @Transactional
    public void accept(AcceptRequest request) {

        TeamInvitation invitation = requireUsable(request == null ? null : request.token());

        if (request.password() == null || request.password().length() < 8 || request.password().length() > 128) {
            throw new IllegalArgumentException("Password must be between 8 and 128 characters");
        }

        if (request.fullName() == null || request.fullName().isBlank() || request.fullName().length() > 120) {
            throw new IllegalArgumentException("Your name is required (max 120 characters)");
        }

        if (userRepository.existsByEmailIgnoreCase(invitation.getEmail())) {
            throw new UserAlreadyExistsException("A user with this email already exists");
        }

        Workspace workspace = workspaceRepository.findWorkspaceById(invitation.getWorkspaceId());

        if (workspace == null) {
            throw new IllegalStateException("This workspace no longer exists");
        }

        planEntitlementService.checkTeamMemberLimit(
                workspace.getId(), userRepository.countByWorkspaceId(workspace.getId()));

        User user = new User();
        user.setWorkspace(workspace);
        user.setFullName(request.fullName().trim());
        user.setEmail(invitation.getEmail());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(invitation.getRole());
        userRepository.save(user);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(Instant.now());
        invitationRepository.save(invitation);
    }

    // ----- helpers ----------------------------------------------------------------------------

    private TeamInvitation requireUsable(String token) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("This invitation link is not valid");
        }

        TeamInvitation invitation = invitationRepository.findByTokenHash(hash(token))
                .orElseThrow(() -> new IllegalArgumentException("This invitation link is not valid"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalArgumentException("This invitation is no longer valid");
        }

        if (invitation.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("This invitation has expired. Ask your admin to send a new one.");
        }

        return invitation;
    }

    private void requireSeatAvailable(UUID workspaceId) {
        long used = userRepository.countByWorkspaceId(workspaceId)
                + invitationRepository.countByWorkspaceIdAndStatus(workspaceId, InvitationStatus.PENDING);
        planEntitlementService.checkTeamMemberLimit(workspaceId, used);
    }

    private TeamInvitation load(UUID id) {
        return invitationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
    }

    private boolean sendEmail(TeamInvitation invitation, String token) {

        String workspaceName = workspaceRepository.findById(invitation.getWorkspaceId())
                .map(Workspace::getName).orElse("your team");

        String link = fidmapConfig.getFrontendUrl() + "/accept-invite?token=" + token;

        String html = "<p>You have been invited to join <strong>" + EmailService.escape(workspaceName)
                + "</strong> on FIDMAP as " + invitation.getRole().name().toLowerCase() + ".</p>"
                + "<p><a href=\"" + link + "\">Accept the invitation</a></p>"
                + "<p>This link expires in 7 days. If you did not expect this, you can ignore this email.</p>";

        try {
            emailService.send(invitation.getEmail(), "You're invited to " + workspaceName + " on FIDMAP", html);
            invitation.setLastEmailError(null);
            invitationRepository.save(invitation);
            return true;
        } catch (EmailDeliveryException e) {
            log.warn("Invitation email failed for invitation {}: {}", invitation.getId(), e.getMessage());
            invitation.setLastEmailError(truncate(e.getMessage()));
            invitationRepository.save(invitation);
            return false;
        }
    }

    private static InvitationView view(TeamInvitation i, boolean emailSent) {
        return new InvitationView(i.getId(), i.getEmail(), i.getRole(), i.getStatus(), i.getCreatedAt(),
                i.getExpiresAt(), i.getStatus() == InvitationStatus.PENDING && i.getExpiresAt().isBefore(Instant.now()),
                emailSent);
    }

    private static String normalizeEmail(String email) {
        if (email == null || !EMAIL.matcher(email.trim()).matches() || email.trim().length() > 254) {
            throw new IllegalArgumentException("A valid email address is required");
        }
        return email.trim().toLowerCase();
    }

    private static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String token) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String truncate(String s) {
        return s == null ? null : s.length() > 300 ? s.substring(0, 300) : s;
    }
}
