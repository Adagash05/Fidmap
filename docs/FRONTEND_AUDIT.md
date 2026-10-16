# FRONTEND AUDIT

React 19 + Vite, fetch-based `Api.js`, react-router. Audit by code reading and esbuild parse (114 files, 0 errors). **No Vite build, ESLint, unit test, browser run or accessibility test was executed.**

## Findings → action
| Finding | Action |
|---|---|
| **Roadmap target date lost**: `RoadmapItemModal` create form never rendered a date field and `Api.roadmapItems.create` never sent `targetDate` | Field shown and sent on create and edit; `feedbackPostIds` also sent on create |
| API wrapper had no pagination, 402 handling or optional-auth mode | `Api.js`: `feedback.page`, `ApiError.subscriptionRequired`, `auth:"optional"`; new modules `decisions`, `duplicates`, `insights`, `notifications`, `invitations`, `surveys`, `platformAdmin` |
| No decision workflow UI | `pages/Decisions/*` (Priorities, DecisionBrief, Duplicates), `Insights`, `Surveys/*` |
| Team invites missing | `TeamInvitations` inside `SettingsTeam`, public `AcceptTeamInvite` |
| No platform directory | `Admin/PlatformWorkspaces` (SUPER_ADMIN) |
| Weak positioning | Home headline "Know what to build next.", CTAs "Start your free trial" / "See how it works", `HowItWorks`, "How FIDMAP helps you decide" section |
| Pricing page overclaimed (Lifetime) | Description corrected; `PlanGuide` added; prices and Polar IDs untouched |
| Comparison content | Four pages on a shared `ComparisonPage`, sourced from COMPETITOR_RESEARCH.md |
| SEO | `Seo` on new pages; sitemap entries; `noIndex` on survey, unsubscribe and invite pages |

## Conventions used in new UI
`useAsync` for loading/error/empty; existing `fm-*` classes and tokens with a `fm-x-*` block appended to `Style.css`; routes registered in every router tree.

## Not done / risks
- No visual QA, responsive check, keyboard/ARIA audit, or Lighthouse run.
- No frontend tests; no real product screenshots on marketing pages (the app cannot run here).
- Hidden `/exclusive-deal` lifetime page pre-exists; Lifetime is commented out of `PLANS`.
- "Priority support" under Business in `pricing.js` pre-exists and is unverified; left as is and flagged.
- Hardcoded GA4 fallback ID in `utils/analytics.js`.
