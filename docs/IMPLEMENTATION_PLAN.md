# IMPLEMENTATION PLAN

Phases follow spec §26; the status is the state at this checkpoint.

| Phase | Scope | State |
|---|---|---|
| 1 Audit | Backend, frontend, security/tenancy audits | Done (by reading); docs written |
| 2 Foundations | Tenant guard, entitlement evaluation, pagination, error mapping | Implemented—unverified |
| 3 Roadmap date | Root cause + fix | Implemented—unverified |
| 4 Decision engine | Scoring v1, inputs, brief, decisions, roadmap creation | Implemented—unverified (engine harness-verified) |
| 5 Evidence | Surveys, duplicates, insights | Implemented—unverified |
| 6 Feedback loop | Followers, notifications, unsubscribe | Implemented—unverified |
| 7 Teams/admin | Invitations, Resend, super-admin, audit log | Implemented—unverified |
| 8 Frontend | Pages, routes, API client, marketing, pricing, comparisons, SEO | Implemented—unverified |
| 9 Tests | 5 new JUnit files; frontend tests not written | Partly; not executed |
| 10 Verify + package | Compile/build/lint blocked; ZIPs built and inspected | Blocked / done |

## Next steps, in order
1. Networked machine: `mvn test`, fix compile and test failures; `npm ci`, lint, build, fix.
2. Run ACCEPTANCE_TESTS.md; update REQUIREMENTS_TRACEABILITY.md statuses to VERIFIED with evidence.
3. Decide the legacy-workspace backfill.
4. Pagination for members, changelog, roadmap; fix `BoardService.editBoard` `isPublic`.
5. Add IDOR integration tests (Testcontainers) and frontend tests.
6. Real screenshots on marketing pages once the app runs.
7. Later: shared rate limiter, integrations (Slack/Linear/Jira) and SSO, as separate scoped projects.
