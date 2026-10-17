# RELEASE NOTES

**Not yet built, tested or run.** Treat this as a release candidate that needs the build gates in ACCEPTANCE_TESTS.md.

## New
- Positioning "Know what to build next": new home headline, CTAs, How it works page, decision-focused sections.
- Explained priorities: versioned score with components, range, confidence and recommendation; Product Decision Brief; recorded decisions with rationale; create a roadmap item from a decision.
- Surveys: build, publish, public answer, results, link to a request as evidence.
- Duplicate suggestions with human confirm/dismiss and vote-safe merge.
- Insights: themes and trends.
- Customer notifications with dedupe, retry and unsubscribe.
- Team invitations by email; super-admin workspace directory and audit log.
- Comparison pages: Canny, Frill, UserVoice, Productboard (sourced, conservative claims).

## Fixed
- Roadmap target date is now shown and saved on create and edit.
- Subscription expiry is enforced by the backend (HTTP 402).
- Cross-workspace access holes closed in board, feedback, comment, vote, roadmap, changelog, workspace and user services.
- Pricing page no longer claims a Lifetime plan.

## Changed
- Opaque `ImpactScoreService` removed in favour of the explained engine.
- Swagger limited to SUPER_ADMIN; rate limits on auth and public writes.

## Breaking / action needed
- Workspaces with no subscription row now lose access until backfilled (DATABASE_AND_MIGRATIONS §4).
- New env vars: `RESEND_FROM`, `FIDMAP_ADMIN_BOOTSTRAP_TOKEN`, `FIDMAP_NOTIFICATION_INTERVAL_MS`.
- Delete three superseded backend files (FILE_REPLACEMENT_MAP.md).
- Rotate the secrets that were in the original `.env`.

## Unchanged
Prices ($19 / $39) and Polar product ids.

## Not included
SSO; third-party integrations and API credentials/webhooks; super-admin workspace creation; frontend tests.

## Production stabilization (2026-10-17)
Status of every item below: **IMPLEMENTED, NOT COMPILED OR RUN** (Maven/npm are blocked in the build sandbox; Java was only syntax-checked with javac, the frontend only parsed with esbuild).

- **Feedback HTTP 500 (provable defect)**: `FeedbackPostRepository.search` used `(:status IS NULL OR f.status = :status)`. With no status filter (the default) PostgreSQL cannot type the NULL enum parameter. Replaced by two explicit queries (`search`, `searchByStatus`).
- **Feedback + Roadmap HTTP 500 (probable cause, needs a log to confirm)**: `feedback_post` carried seven unused `NOT NULL` primitive columns. On a database that already had rows `ddl-auto: update` cannot add such columns, so every query selecting `feedback_post` fails; the roadmap does this through each item's linked requests. Columns are now boxed, nullable, with DB defaults (SQL in DATABASE_AND_MIGRATIONS.md §7). **If the 500s persist after deploy, send the stack trace; the cause is then something else.**
- **Survey limits**: new `plans.max_surveys` (Startup 1, Business 10, Lifetime 10 = Business tier). Enforced in `SurveyService.createSurvey` via `PlanEntitlementService.checkSurveyLimit` (active-access check first). Existing plan rows are backfilled on start. Exposed as `maxSurveys` in `/api/billing/entitlements`. Surveys page shows `n / limit used`, disables New survey with an explanation, and offers the upgrade link only on Startup.
- **Plan-limit errors are no longer HTTP 400**: `PlanLimitExceededException` -> 403 with `errorCode: "PLAN_LIMIT"`; expired subscriptions -> 402 with `errorCode: "SUBSCRIPTION_<REASON>"`. `ApiError` exposes `planLimit` / `subscriptionRequired` so the UI can tell them from server failures.
- **Subscription expiry**: backend enforcement already existed (`requireActiveAccess` on every write; deletes intentionally allowed; billing endpoints and `/entitlements` stay reachable). Added the persistent `SubscriptionBanner` (staff app, all pages) driven by `accessActive`/`accessReason`. Lifetime and active plans never show it.
- **Invitation email**: empty `RESEND_FROM` used to produce a blank sender (Spring only applies a default when the key is absent), and the default Resend test sender can only deliver to the Resend account owner. Blank now falls back to the test sender with a startup WARN; password reset uses the same sender; the invitation response carries `emailError` and the Team page shows the real provider message instead of a generic one. **A production sender on a verified domain (`RESEND_FROM`) and `FIDMAP_FRONTEND_URL=https://app.fidmap.co` must be set.**
- Tests added to `PlanEntitlementEvaluateTest` (survey limits, downgrade, expiry order, initializer backfill). No new test files.
