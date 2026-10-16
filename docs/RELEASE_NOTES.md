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
