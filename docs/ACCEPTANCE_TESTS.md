# ACCEPTANCE TESTS

All cases below are **manual and NOT YET EXECUTED** (the app cannot run in the authoring sandbox). Record the result and date next to each case when you run it. Use two workspaces, A and B, each with an OWNER, and one expired-trial workspace C.

## Automated (written, not executed)
`mvn test` should run: `PriorityScoringEngineTest`, `TextSimilarityTest`, `PlanEntitlementEvaluateTest`, `SurveyValidatorTest`, `RateLimitFilterTest` (plus the pre-existing `FidmapApplicationTests`, which needs a database). A JDK-only harness over the first two classes ran in the authoring session (30 checks, 0 failures); that is not a JUnit run.

## Roadmap target date (REQ-6) — regression
1. Sign in as OWNER of A. Open Roadmap, click add item.
2. Expect a **Target date** field in the create form. Pick a future date, save.
3. Expect the card to show that date. Reload the page: still shown.
4. Edit the item, change the date, save, reload: new date shown. Clear it: no date shown.
5. Create an item from a feedback request via the decision flow with a target date: date saved.
6. API check: the roadmap-item create call (`roadmapItems.create` in `Api.js`) with `targetDate` returns it in the response.

## Subscription expiry (REQ-15)
1. Workspace C (trial ended, no paid subscription): any staff write (create board, post, decide) returns HTTP 402 and the UI shows the upgrade prompt.
2. Workspace with active subscription: normal.
3. Canceled but period not ended: access until the period end, then 402. No grace period after expiry.
4. Workspace with no subscription row: 402 (fails closed).
5. Replay an old Polar webhook with an older `modified_at`: status does not regress.

## Tenancy / IDOR (REQ-21)
As OWNER of B, call with a resource id from A and expect 403 or 404 for: board get/edit/delete, feedback get/edit/delete/page, comment edit/delete, vote, roadmap item get/edit/delete, changelog get/edit/delete, survey get/edit/results, decision brief and decide, duplicates list/merge, notification list/retry, invitation list/revoke, user and member management. Also: `/api/admin/**` as a non-SUPER_ADMIN returns 403; Swagger as non-SUPER_ADMIN returns 401/403.

## Decision workflow (REQ-5, 7, 8)
1. Priorities list shows score, range, confidence and recommendation per request, paged.
2. Open a brief: components with weights, evidence counts, linked surveys, merged requests.
3. Set urgency/strategic/effort; the score updates; effort is shown but does not change the score.
4. Decide with the recommended action: saved. Choose a different action or REJECT without rationale: rejected with a validation message. With rationale: saved with snapshot (score, confidence, version).
5. Decide PRIORITIZE with "create roadmap item": the roadmap item exists and is linked, with its target date.

## Surveys (REQ-9)
Create with each question type and a required question; publish; open the public link signed out (an email is asked); submit with a required answer missing: blocked; submit valid: accepted; submit again as the same user: refused. Draft survey public link: not available. Close: link refuses. Results show counts and ratings; link to a request and see it in that brief.

## Duplicates (REQ-10)
Create two near-identical requests, scan, see the pair with a reason, confirm: votes moved once (no double count), comments kept, the old request marked merged. Dismiss: pair not suggested again.

## Notifications (REQ-12)
Follow a request, change its status: one email. Repeat the same event: no second email. Fail the provider (bad key): status FAILED with an error, retry works; status is never SENT without a provider id. Unsubscribe link works without login.

## Invitations and admin (REQ-16)
Invite an email: email arrives, link opens the accept page; accept sets a password and joins A with the role. Reuse the link: refused. After 7 days: refused. Resend: the old link stops working. Revoke: refused. Only MEMBER and ADMIN can be invited; only an OWNER can invite an ADMIN. SUPER_ADMIN sees the workspace directory and the audit list; others cannot.

## Marketing / SEO (REQ-4, 14, 19, 20)
Home headline reads "Know what to build next."; CTAs "Start your free trial" and "See how it works" work; `/how-it-works`, `/pricing`, `/alternatives/*` load and render a title and description; `sitemap.xml` lists them; survey, unsubscribe and invite pages carry `noindex`. Pricing amounts match `constants/pricing.js` and the Polar product configuration.

## Build gates (run first)
`mvn -q test` · `npm ci && npm run lint && npm run build`.
