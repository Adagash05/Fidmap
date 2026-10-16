# REFACTOR PROGRESS (checkpoint)

Last updated: 2026-10-15. Statuses: NOT STARTED / IN PROGRESS / BLOCKED / COMPLETE (spec labels in REQUIREMENTS_TRACEABILITY.md).

## Working copies
Backend `be/` (Maven root `be/fidmap`), frontend `fe/`. Originals untouched in `be_orig/`, `fe_src/`.

## Completed in code (not COMPLETE: unverified, see traceability)
Subscription enforcement; tenant guards and hardened services; roadmap date fix; decision engine, brief and decisions; duplicates; insights; surveys end to end; notifications; invitations; audit log; super-admin directory; pagination (partial); marketing/pricing/comparison/SEO; the 13 docs; two ZIPs.

## Tests
- Written, not executed: `PriorityScoringEngineTest`, `TextSimilarityTest`, `PlanEntitlementEvaluateTest`, `SurveyValidatorTest`, `RateLimitFilterTest`.
- Executed: a JDK-only harness over `PriorityScoringEngine` and `TextSimilarity` (30 checks, 0 failures); `javac` parse-only over backend; esbuild parse/bundle over frontend (114 files, 0 errors).
- Not executed and why: Maven compile/test and npm lint/build, because Maven Central and the npm registry are blocked (403). No workaround was attempted.

## Migrations
SQL is in DATABASE_AND_MIGRATIONS.md, not executed. `ddl-auto: update` creates the tables and columns at startup.

## BLOCKED
Compile/test/build/lint (network). App run and screenshots (no runtime).

## DEFERRED WITH JUSTIFICATION
SSO; third-party integrations and API credentials/webhooks; super-admin workspace creation.

## Known bugs / risks
See "Known gaps" in REQUIREMENTS_TRACEABILITY.md.

## Exact next step
On a networked machine: copy the ZIPs over the projects, run `mvn -q test` and fix compile errors, run `npm ci && npm run lint && npm run build` and fix, then apply ACCEPTANCE_TESTS.md manually, starting with the roadmap-date regression and subscription-expiry cases. Then mark each requirement VERIFIED with the run output as evidence.
