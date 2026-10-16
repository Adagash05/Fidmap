# REQUIREMENTS TRACEABILITY

IDs follow the section numbers of the master specification (REQ-4 = spec §4, and so on). Priority: **P0** = release blocker, **P1** = core, **P2** = supporting.

## Status legend
NOT STARTED · IN PROGRESS · IMPLEMENTED—UNVERIFIED (code written and syntax/parse-checked, not compiled/run) · VERIFIED (executed, with evidence cited) · BLOCKED · DEFERRED WITH JUSTIFICATION.

## What "evidence" exists in this session (read this first)
| Check | Result |
|---|---|
| `mvn compile` / `mvn test` / JUnit | **NOT RUN.** Maven Central is blocked from this sandbox (policy 403). |
| `vite build` / `eslint` / `npm test` | **NOT RUN.** The npm registry is blocked. |
| `javac` syntax-only parse of all backend sources (`syn.sh`) | Run; no syntax errors. This is **not** compilation (no classpath, no type-checking against Spring/Lombok). |
| JDK-only harness over the pure classes `PriorityScoringEngine` and `TextSimilarity` | Run; 30 checks, 0 failures. **VERIFIED for those two classes only.** The JUnit tests in the repo were not run. |
| esbuild parse of all frontend files | Run; 114 files, 0 errors; bundle resolves all local imports. **Not** a Vite build, lint or runtime test. |
| App started / browser UI exercised / screenshots | **NOT DONE.** The app cannot be run here. |

Consequence: apart from the two pure classes above, nothing in this file is VERIFIED.

## Matrix
| ID | Pri | Requirement | Status | Implementation (main files) | Evidence / gap |
|---|---|---|---|---|---|
| REQ-4 | P1 | USP "Know what to build next"; decision-workflow positioning | IMPLEMENTED—UNVERIFIED | FE `pages/Marketing.jsx`, `HowItWorks.jsx`, `layouts/MarketingLayout.jsx`, `StaffDashboard.jsx`, `Header.jsx` | esbuild parse only. No real product screenshots (app cannot run). |
| REQ-5 | P1 | Solves a real problem end to end | IMPLEMENTED—UNVERIFIED | Evidence → score → brief → decision → roadmap: `decision/*`, FE `pages/Decisions/*` | Not exercised end to end. |
| REQ-6 | P0 | Roadmap target-date bug | IMPLEMENTED—UNVERIFIED | FE `RoadmapItemModal.jsx`, `Api.js` (`roadmapItems.create` sends `targetDate`, `feedbackPostIds`); BE `AddItemRequest`, `RoadmapItemDto/Mapper/Service` | Root cause found by reading: the create form never rendered or sent the date and the API wrapper never sent it. Manual regression steps in ACCEPTANCE_TESTS.md. No automated test. |
| REQ-7 | P1 | Explainable prioritization | IMPLEMENTED—UNVERIFIED; engine VERIFIED by harness | `decision/PriorityScoringEngine`, `EvidenceCollector`, `DecisionService`, `DecisionController`; FE `Priorities.jsx` | Engine: 30 harness checks pass. JUnit `PriorityScoringEngineTest` written, not run. Collector JPQL not run against a DB. |
| REQ-8 | P1 | Product Decision Brief | IMPLEMENTED—UNVERIFIED | `DecisionService.brief/decide`, `ProductDecision`; FE `DecisionBrief.jsx` | Decision snapshots score, confidence, version, evidence. Override/REJECT require rationale. |
| REQ-9 | P1 | Surveys (build, publish, answer, results, link to request) | IMPLEMENTED—UNVERIFIED | `surveyFile/*`, `PublicSurveyController`, `SurveyValidator`; FE `pages/Surveys/*` | `SurveyValidatorTest` written, not run. |
| REQ-10 | P1 | Duplicate detection and merge | IMPLEMENTED—UNVERIFIED; similarity VERIFIED by harness | `duplicate/*`; FE `Duplicates.jsx` | Human confirm/dismiss. Scan capped at 1500 posts per workspace. Merge not run against a DB. |
| REQ-11 | P2 | Insights and themes | IMPLEMENTED—UNVERIFIED | `insight/*`; FE `Insights.jsx` | Themes are shared title terms (simple, deterministic; not ML). |
| REQ-12 | P1 | Customer feedback loop / notifications | IMPLEMENTED—UNVERIFIED | `notification/*`, `mail/EmailService` | SENT only with a provider message id; dedupe; retry; public unsubscribe. Not sent against Resend. |
| REQ-13 | P1 | Consistent production UI/UX | IN PROGRESS | New pages use the existing `fm-*` design tokens (`Style.css` `fm-x-*`); loading/error/empty via `useAsync` | No visual review or accessibility audit was possible. See FRONTEND_AUDIT.md. |
| REQ-14 | P1 | Plans, pricing and perceived value | IMPLEMENTED—UNVERIFIED | FE `constants/pricing.js`, `Pricing.jsx`, `PlanGuide.jsx`; `PRICING_RESEARCH.md` | Prices and Polar IDs **unchanged**. Research is a proposal only. "Priority support" under Business pre-exists and is unverified. |
| REQ-15 | P0 | Subscription expiry enforcement | IMPLEMENTED—UNVERIFIED | `PlanEntitlementService.evaluate/requireActiveAccess`, `SubscriptionRequiredException` (402), guards in services; FE `ApiError.subscriptionRequired` | `PlanEntitlementEvaluateTest` written, not run. Fails closed; no grace period. Legacy workspaces without a row now lose access; see DATABASE_AND_MIGRATIONS §4. |
| REQ-16a | P1 | Team invitations | IMPLEMENTED—UNVERIFIED | `invitation/*`; FE `TeamInvitations.jsx`, `AcceptTeamInvite.jsx` | SHA-256 hashed single-use tokens, 7-day TTL, resend issues a new token, revoke. |
| REQ-16b | P1 | Resend email | IMPLEMENTED—UNVERIFIED | `mail/EmailService`, `EmailDeliveryException`, `RESEND_FROM` | Never marks SENT without a provider id. Not sent live. |
| REQ-16c | P1 | Super-admin directory | IMPLEMENTED—UNVERIFIED | `admin/AdminController`, `AuditService`; FE `PlatformWorkspaces.jsx`; bootstrap via `FIDMAP_ADMIN_BOOTSTRAP_TOKEN` | Super-admin workspace creation DEFERRED WITH JUSTIFICATION: registration already creates a workspace plus trial. |
| REQ-17 | P2 | Pagination and data integrity | IN PROGRESS | `common/PageResponse`, `Paging`; paged: feedback, priorities, duplicates, notifications, admin lists, survey responses | Members, changelog and roadmap lists are not paged. |
| REQ-18a | P2 | Integrations (foundations only) | DEFERRED WITH JUSTIFICATION | Foundations: notification outbox, audit log, per-workspace authorization | No third-party integration is implemented and none is advertised. API credentials/webhooks are not implemented. |
| REQ-18b | — | SSO | DEFERRED WITH JUSTIFICATION | — | Spec says full SSO is deferred. |
| REQ-19 | P1 | Competitor research and comparison pages | IMPLEMENTED—UNVERIFIED | `COMPETITOR_RESEARCH.md`; FE `Alternatives/*` (Canny, Frill, UserVoice, Productboard) | Facts checked 2026-10-14; only conservative claims used; each page cites its source. |
| REQ-20 | P1 | SEO, marketing, analytics | IMPLEMENTED—UNVERIFIED | `Seo` on new pages, `public/sitemap.xml`, `noIndex` on utility pages | Hardcoded GA4 fallback ID in `utils/analytics.js` noted in audits (events carry no PII). |
| REQ-21 | P0 | Security and multitenancy | IMPLEMENTED—UNVERIFIED | `WorkspaceAuthorizationService`, hardened services, `SecurityConfig`, `RateLimitFilter`, `EndUserResolver`, Polar webhook hardening | Findings in SECURITY_AND_TENANCY_AUDIT.md. No integration/IDOR tests were run. Rate limiter is in-memory (single instance). |
| REQ-22 | P1 | Migrations and backward compatibility | IMPLEMENTED—UNVERIFIED | DATABASE_AND_MIGRATIONS.md (idempotent SQL) | SQL not executed. |
| REQ-23 | P0 | Automated tests and actual verification | BLOCKED (partly) | 5 new JUnit files in `src/test/java`; `spring-boot-starter-test` added to `pom.xml` | Cannot be executed here. Harness evidence above. No frontend tests written. |
| REQ-24 | P0 | Copy-back ZIPs and file map | IMPLEMENTED—UNVERIFIED | FILE_REPLACEMENT_MAP.md; two ZIPs | ZIP contents inspected and checked; see final response. |
| REQ-25 | P0 | Release gates | See "Release gates" below | | |

## Release gates (spec §25)
- No claim of passing tests/builds without execution: **honoured**; the table above lists what ran.
- No fabricated tests, prices, competitor claims, testimonials, screenshots, links: **honoured**.
- Full SSO deferred; integrations limited to foundations and not advertised: **honoured**.
- Live Polar IDs and prices unchanged: **honoured** (`constants/pricing.js` diff adds feature bullets only).
- Entitlements enforced on the backend: implemented, unverified.
- No secrets or customer data in deliverables: ZIPs exclude `.env`; scan result is in the final response.
- Originals not overwritten; no production migration executed: **honoured**.

## Known gaps (carry forward)
1. Compile, JUnit, Vite build, ESLint have never been run on this code. First action on a machine with network access: `mvn -q test` and `npm ci && npm run lint && npm run build`, then fix whatever surfaces.
2. `BoardService.editBoard` still nulls `isPublic` when the field is omitted.
3. Pagination for members, changelog and roadmap.
4. `SettingsSubscription` wording should be checked against the effective status values.
5. Frontend automated tests.
6. Real screenshots for marketing pages.
7. Unverified-email end-user limitation; in-memory rate limiter; the Polar `order.refunded` handling is an assumption to confirm against Polar's payload.
