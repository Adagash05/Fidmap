# BACKEND AUDIT

Spring Boot 4.1, Java 21, Postgres, JPA with `ddl-auto: update`, stateless JWT, Lombok, Polar billing, Resend email. Audit by code reading; nothing compiled (Maven blocked).

## Original findings → action
| Area | Finding | Action |
|---|---|---|
| Authorization | No central tenant guard | `WorkspaceAuthorizationService`; applied to board, feedback, comment, vote, roadmap, changelog, workspace, user, survey services |
| Billing | Status stored from the provider only; expiry never evaluated | Pure `PlanEntitlementService.evaluate(subscription, now)`; effective status exposed in responses; 402 on write/use |
| Prioritization | `ImpactScoreService` (opaque score) | **Removed** with `FeedbackPriorityResponse`; replaced by the versioned `PriorityScoringEngine` (v1) with components, range, confidence, recommendation |
| Surveys | Entities and endpoints without a usable public flow | Publish/close, public answer, validation, results, link to a request |
| Notifications | Absent / unreliable | Outbox table, dedupe, retry, honest status |
| Pagination | Unbounded list endpoints | `PageResponse`/`Paging` for feedback, priorities, duplicates, notifications, admin, survey responses (members, changelog, roadmap remain unpaged) |
| Errors | Inconsistent | `GlobalExceptionHandler` maps 402/403/404/validation |
| Tests | None | 5 new JUnit files (not run) |

## Scoring engine v1 (pure and deterministic)
Weights: demand 40, survey 20, recency 10, urgency 15, strategic 15. Demand = 100·ln(1+n)/ln(21). Survey = count score ln(1+c)/ln(11) blended 50/50 with the rating score. Recency has a 30-day half-life. Human inputs 1–5 map to (v−1)·25. Missing components are excluded and the rest renormalized; the low/high range treats missing as 0/100. Confidence HIGH if supporters ≥ 10 and ≥ 2 of {survey, urgency, strategic}; MEDIUM if supporters ≥ 10, or ≥ 3 with ≥ 1 of those; else LOW. Recommendation INVESTIGATE if LOW or (supporters < 3 and no survey); PRIORITIZE ≥ 65; CONSIDER ≥ 40; else DEFER. REJECT is human-only. Effort is displayed beside the score, not inside it.

## Remaining backend debt
- `BoardService.editBoard` nulls `isPublic` if omitted.
- N+1 risk remains in older list endpoints; the new decision path uses bulk JPQL (`EvidenceCollector`), unmeasured.
- Duplicate scan is capped at 1500 posts per workspace and is O(n²) on those.
- No Flyway; see DATABASE_AND_MIGRATIONS.md.
- Verification: compile + `mvn test` still to be run on a networked machine.
