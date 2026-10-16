# SECURITY AND TENANCY AUDIT

Method: code reading of controllers, services, repositories and `SecurityConfig` in the working copy, plus fixes. **No penetration test, no integration test and no IDOR test was executed** (the app cannot be built or run here). Every "fixed" item is IMPLEMENTED—UNVERIFIED.

## Tenancy model
Every tenant-owned row carries `workspace_id` (directly or through its board/post). Staff roles: SUPER_ADMIN, OWNER, ADMIN, MEMBER; PARTNER is referral-only. Central guard: `WorkspaceAuthorizationService` (`requireStaff`, `requireManager`, `requireStaffWrite`, `isStaffOf`). Services resolve the owning workspace **from the resource** (never from a client-supplied workspace id alone) and then call the guard.

## Findings and fixes
| # | Finding (in the original code) | Risk | Fix | Status |
|---|---|---|---|---|
| S1 | Board, feedback, comment, vote, roadmap, changelog, workspace and user services accepted resource ids without checking the caller's workspace | Cross-workspace read/write (IDOR) | Guard calls added in each service; resource → workspace resolution | IMPLEMENTED—UNVERIFIED |
| S2 | Subscription expiry was not enforced on the server | Expired workspaces kept full access | `PlanEntitlementService.evaluate` (pure) + `requireActiveAccess`; 402 `SubscriptionRequiredException`; fails closed, no grace period | IMPLEMENTED—UNVERIFIED |
| S3 | Public end-user identity was not tied to a workspace | One end user could act across workspaces | `EndUserResolver` links end users to the workspace; `EndUserController` removed | IMPLEMENTED—UNVERIFIED |
| S4 | Swagger/OpenAPI reachable without auth | Information disclosure | Restricted to SUPER_ADMIN in `SecurityConfig` | IMPLEMENTED—UNVERIFIED |
| S5 | No rate limiting on auth and public write endpoints | Brute force, spam | `RateLimitFilter`: 10/min auth, 40/min public writes, per IP | IMPLEMENTED—UNVERIFIED; **in-memory, per instance** |
| S6 | Client IP behind a proxy | Rate limit keyed on the proxy | `server.forward-headers-strategy: framework` | Config only; verify your proxy sets `X-Forwarded-For` and is the only ingress, or clients can spoof it |
| S7 | Polar webhook handling | Forged or replayed events | Signature verification kept; stale events ignored via `provider_modified_at`; unknown events do not grant access | IMPLEMENTED—UNVERIFIED; `order.refunded` handling is an assumption to confirm with Polar's payload |
| S8 | Super-admin creation | Privilege escalation | Only via `FIDMAP_ADMIN_BOOTSTRAP_TOKEN` (empty disables); `/api/admin/**` requires SUPER_ADMIN; manual role check in `AdminController` (an earlier `@PreAuthorize` was inert and was replaced) | IMPLEMENTED—UNVERIFIED |
| S9 | Invitations | Token theft/reuse | Token shown once in the email link, stored only as SHA-256; single use; 7-day TTL; resend rotates; revoke; only MEMBER or ADMIN can be invited, and only an OWNER can invite an ADMIN | IMPLEMENTED—UNVERIFIED |
| S10 | Notifications | Spam / false "sent" | Dedupe per (post, user, event); unsubscribe token; SENT only with a provider message id | IMPLEMENTED—UNVERIFIED |
| S11 | Sensitive actions unrecorded | No accountability | `AuditLog` for decisions, merges, invitations; viewable by SUPER_ADMIN | IMPLEMENTED—UNVERIFIED |
| S12 | Surveys | Anonymous flooding, cross-tenant answers | Public survey endpoints verify the survey is published and in the stated workspace; one response per end user; validator checks question/option ownership | IMPLEMENTED—UNVERIFIED |

## Open risks (not fixed)
1. A real `.env` with secrets sits in the original archive. It is excluded from the new ZIPs. **Rotate those secrets**: they were shared in an archive.
2. Rate limiter is in memory: multiple instances multiply the limit and a restart resets it. Use a shared store (Redis) for production at scale.
3. Public end users are identified by the email they type, which is not verified. Anyone can post or vote as an address. This is a product limitation, not fixed.
4. CSRF is disabled, which is acceptable for a stateless bearer-token API; do not add cookie auth without re-enabling it.
5. GA4 measurement ID fallback is hardcoded in `utils/analytics.js`. It is an identifier, not a secret. Events carry no personal data and page views send pathname only.
6. Legacy workspaces with no subscription row are locked out (fail closed). See DATABASE_AND_MIGRATIONS §4.
7. JWT secret strength and CORS origins depend on deployment configuration (`JWT_SECRET_KEY`, `FIDMAP_FRONTEND_URL`); not verified.
8. Dependency vulnerability scan was not possible (registries blocked).

## Recommended tests (written as manual cases in ACCEPTANCE_TESTS.md; automated IDOR tests are not yet written)
For each id-based endpoint: authenticate as workspace B and request a workspace A resource; expect 403/404.
