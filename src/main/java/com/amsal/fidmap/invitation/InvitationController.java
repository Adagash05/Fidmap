package com.amsal.fidmap.invitation;

import com.amsal.fidmap.apiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping("/api/invitations/workspace/{workspaceId}")
    public ResponseEntity<ApiResponse<InvitationService.InvitationView>> invite(
            @PathVariable UUID workspaceId, @RequestBody InvitationService.InviteRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.success("invitation created", invitationService.invite(workspaceId, request)));
    }

    @GetMapping("/api/invitations/workspace/{workspaceId}")
    public ResponseEntity<ApiResponse<List<InvitationService.InvitationView>>> list(@PathVariable UUID workspaceId) {
        return ResponseEntity.ok(ApiResponse.success("invitations", invitationService.list(workspaceId)));
    }

    @PostMapping("/api/invitations/{id}/resend")
    public ResponseEntity<ApiResponse<InvitationService.InvitationView>> resend(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("invitation re-sent", invitationService.resend(id)));
    }

    @DeleteMapping("/api/invitations/{id}")
    public ResponseEntity<ApiResponse<Void>> revoke(@PathVariable UUID id) {
        invitationService.revoke(id);
        return ResponseEntity.ok(ApiResponse.success("invitation revoked", null));
    }

    @PostMapping("/api/public/invitations/preview")
    public ResponseEntity<ApiResponse<InvitationService.Preview>> preview(@RequestBody java.util.Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success("invitation", invitationService.preview(body.get("token"))));
    }

    @PostMapping("/api/public/invitations/accept")
    public ResponseEntity<ApiResponse<Void>> accept(@RequestBody InvitationService.AcceptRequest request) {
        invitationService.accept(request);
        return ResponseEntity.ok(ApiResponse.success("invitation accepted", null));
    }
}
