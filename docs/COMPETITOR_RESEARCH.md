# FIDMAP Competitor Research

Research date: 2026-10-14. Competitors: Canny, Frill, UserVoice, Productboard.

## Method and reliability caveats (read first)

- Sources were fetched with WebFetch, which returns a model-generated summary of the page. It is not raw HTML. Direct `curl` to the competitor domains was blocked by the session proxy (HTTP 403 on CONNECT), so no raw page text was available to cross-check.
- Pricing tables on these sites are rendered as flattened tables, so per-plan feature checkmarks often could not be mapped to a plan. Those cells are marked UNVERIFIED rather than guessed.
- Third-party "pricing guide" blogs appeared in search results. They are NOT official, were not fetched, and are not used for any fact here.
- Every fact below was retrieved on 2026-10-14. "$" is shown by the pages without a currency code. Treat it as USD only as an assumption (UNVERIFIED currency code).
- Re-verify every figure by eye in a browser before publishing any comparison page.

---

## 1. Canny (canny.io)

**Target audience (as positioned on the pages fetched):** SaaS product teams collecting public/private feedback via voting boards. Contributors are free and unlimited. Pricing scales with tracked users.

### Verified facts

| Fact | Value | Source | Date |
|---|---|---|---|
| Plans | Free, Pro, Business | https://canny.io/pricing | 2026-10-14 |
| Free | $0/month, 25 tracked users, 5 managers, unlimited contributors | https://canny.io/pricing | 2026-10-14 |
| Pro | "$79 / month billed yearly", 100+ tracked users, 10 managers | https://canny.io/pricing | 2026-10-14 |
| Business | "Custom" price, "Request demo" (contact sales), 5,000+ tracked users | https://canny.io/pricing | 2026-10-14 |
| Pro price lower bound | Page says "Save up to 20% by paying for a year upfront". A plain monthly-billing price was NOT shown. | https://canny.io/pricing | 2026-10-14 |
| Pricing model | Per tracked user (any user associated with feedback), tiered. Optional monthly spend limit. Without one, exceeding a limit auto-upgrades with a prorated charge. | https://canny.io/pricing | 2026-10-14 |
| Trial | Pro has a "Start trial" button. Trial length not stated. | https://canny.io/pricing | 2026-10-14 |
| Boards | Unlimited on all listed tiers | https://canny.io/pricing | 2026-10-14 |
| Feedback volume | Unlimited | https://canny.io/pricing | 2026-10-14 |
| AI | "Autopilot": feedback discovery, deduplication, comment summaries, smart replies. Described as included in all plans. | https://canny.io/pricing | 2026-10-14 |
| Duplicate merging | Yes (Autopilot deduplication; FAQ says duplicates are merged) | https://canny.io/pricing | 2026-10-14 |
| Integrations named | Capture: Intercom, Zendesk, Slack, Chrome Extension. Notifications: Slack, Microsoft Teams, Discord. PM tools: Jira, ClickUp, Linear, Asana, GitHub, Azure DevOps. Also Zapier, HubSpot, Salesforce. | https://canny.io/pricing | 2026-10-14 |
| SSO | Business card lists "SSO integrations" (Okta, OIDC, Entra ID, OneLogin). Page is inconsistent on whether SSO is also a base feature. | https://canny.io/pricing | 2026-10-14 |
| API | An API reference exists (linked from developers site) | https://developers.canny.io | 2026-10-14 |
| Voting boards | Help center: "Collect and organize feedback publicly with voting boards" | https://help.canny.io | 2026-10-14 |

### Verified strengths
- Free tier exists (25 tracked users) and contributors are unlimited.
- Broad named integration list, including CRM and project-management tools.
- AI deduplication and summaries are stated as included in all plans.

### Honest trade-offs vs FIDMAP's connected workflow positioning
- Canny's page presents feedback collection, deduplication and integrations. On the fetched page we did not see a "decision brief" or evidence-to-priority workflow. Absence on one summarized page does not prove absence in the product. Do not claim Canny lacks it.
- Canny has a larger verified integration list than FIDMAP is known to have (FIDMAP's integrations were not part of this task).
- Canny's free tier is a real advantage for small teams versus FIDMAP's 7-day trial.

### UNVERIFIED
- Per-plan placement of: public roadmap, changelog, private boards, custom domain, branding removal, API/webhooks, SSO.
- Surveys: not mentioned on the pricing page.
- Prioritization/scoring: not mentioned on the pricing page.
- Trial length; monthly (non-annual) price of Pro; Business price.
- Webhooks: not confirmed on the developers page.
- Currency code.

---

## 2. Frill (frill.co)

**Target audience:** Small to mid SaaS teams wanting a low-cost feedback board, roadmap and announcements with flat pricing.

### Verified facts

| Fact | Value | Source | Date |
|---|---|---|---|
| Plans and monthly prices | Startup $25/mo, Business $49/mo, Growth $149/mo (marked "Popular"), Enterprise $349/mo ("Talk to sales" CTA) | https://frill.co/pricing | 2026-10-14 |
| Annual pricing | A "Billed Yearly" toggle appears, but yearly prices/discount were NOT visible. UNVERIFIED. | https://frill.co/pricing | 2026-10-14 |
| Free tier | None listed | https://frill.co/pricing | 2026-10-14 |
| Trial | "14-day free trial. No credit card required." | https://frill.co/pricing | 2026-10-14 |
| Pricing model | Flat monthly per plan. "Unlimited admins and unlimited widgets on every paid tier." Teammates and tracked users unlimited on all plans. | https://frill.co/pricing | 2026-10-14 |
| Active ideas | Startup 50; Business, Growth, Enterprise unlimited | https://frill.co/pricing | 2026-10-14 |
| Surveys | Startup 1, Business 3, Growth and Enterprise unlimited. Types: NPS, CSAT, Open Feedback, Quick Poll, Idea Poll. | https://frill.co/pricing | 2026-10-14 |
| Roadmap | Roadmap features listed (feature images, drag-and-drop, micro roadmaps, ETA). Timeline View and some items "Coming Soon". | https://frill.co/pricing | 2026-10-14 |
| Announcements | "Announcements" (scheduled, boosted, segmented, multi-language). Closest to a changelog. The word "changelog" was not confirmed. | https://frill.co/pricing | 2026-10-14 |
| Duplicates | "Similar Idea detection" and "Smart merge" | https://frill.co/pricing | 2026-10-14 |
| Prioritization | "Customize Priority Factors" and "Priority Matrix" | https://frill.co/pricing | 2026-10-14 |
| Enterprise extras | SOC2, audit logs, IP whitelisting, dedicated hosting (tier exclusivity not confirmed) | https://frill.co/pricing | 2026-10-14 |
| Privacy / White labeling | Growth card lists Privacy, Surveys, White Labeling. The page also describes these as add-ons. Whether Growth includes them or they cost extra is unclear. Add-on prices not shown. | https://frill.co/pricing | 2026-10-14 |

### Verified strengths
- Lowest verified entry price of the four ($25/mo) and a no-card 14-day trial.
- Unlimited teammates and tracked users on every plan.
- Surveys, similar-idea detection and a priority matrix are listed.

### Honest trade-offs vs FIDMAP
- Frill's Startup caps active ideas at 50, versus FIDMAP Startup's 1,000 feedback posts. This is a limit comparison, not an equivalent definition ("active ideas" vs "posts"). Do not equate them.
- Frill has unlimited seats; FIDMAP caps team members (3 / 10). On seat-heavy teams Frill is more generous.
- Frill lists a priority matrix. FIDMAP's differentiation must rest on evidence-backed decision briefs and the connected workflow, not on "having prioritization".

### UNVERIFIED
- Annual prices; per-plan SSO, API, webhooks, custom domain, AI features (only "AI Inbox" and "MCP" appeared as nav links); integrations (a page is linked but none named); boards limit; whether Privacy and White Labeling are included in Growth; currency code; FAQ content.
- Note: the Enterprise card shows both "$349/mo" and "Talk to sales". Whether $349 is a firm price is UNVERIFIED.

---

## 3. UserVoice (uservoice.com)

**Target audience:** Mid-market and enterprise product organizations (inferred from sales-led pricing and a compliance emphasis; the inference itself is UNVERIFIED).

### Verified facts

| Fact | Value | Source | Date |
|---|---|---|---|
| Plan names | Not listed | https://www.uservoice.com/pricing | 2026-10-14 |
| Prices | Not listed. Directs visitors to "Talk to an Expert". CONTACT SALES / hidden. | https://www.uservoice.com/pricing | 2026-10-14 |
| Pricing model | Usage-based: "Your plan is based on feedback volume, tools leveraged and the integrations you connect, never by seat." | https://www.uservoice.com/pricing | 2026-10-14 |
| Trial | 30-day trial after booking a demo | https://www.uservoice.com/pricing | 2026-10-14 |
| Duplicates | FAQ says it "de-duplicates ideas, aggregates feedback" | https://www.uservoice.com/pricing | 2026-10-14 |
| Prioritization | Lets PMs "segment and weigh an idea's impact" using customer data | https://www.uservoice.com/pricing | 2026-10-14 |
| API | Bulk import via API mentioned; Developer Docs link present | https://www.uservoice.com/pricing | 2026-10-14 |
| Integrations | "Integrations Library" linked; none named on this page | https://www.uservoice.com/pricing | 2026-10-14 |

### Verified strengths
- No per-seat charge stated.
- Duplicate handling and impact weighting are stated.

### Honest trade-offs vs FIDMAP
- No public pricing and a demo-gated trial. A transparent self-serve price and instant trial is a real FIDMAP contrast, but only state what is verified (see "safe claims").

### UNVERIFIED
- All prices and plan names; roadmap, changelog, surveys, SSO, webhooks, AI features (only a use-case link), named integrations, limits, security certifications (page says "Top tier security and compliance" with no details).

---

## 4. Productboard (productboard.com)

**Target audience:** Product management teams needing a full product-management system (roadmaps, objectives, release planning), larger than feedback-only tools.

### Verified facts

| Fact | Value | Source | Date |
|---|---|---|---|
| Plans | Business, Enterprise | https://www.productboard.com/pricing/ | 2026-10-14 |
| Plan prices | Not shown. Business "Request demo", Enterprise "Contact sales". CONTACT SALES / hidden. | https://www.productboard.com/pricing/ | 2026-10-14 |
| Annual discount | Monthly/Annual toggle with "Save 21%" for annual | https://www.productboard.com/pricing/ | 2026-10-14 |
| Pricing model | Per maker; minimum 5 makers on both plans. Contributors do not count toward maker limit. Business includes 25 contributors; Enterprise unlimited. | https://www.productboard.com/pricing/ | 2026-10-14 |
| Credits (AI usage) | Business 500 credits per maker per month. Enterprise 1,500 base plus 800 per additional maker per month. Top-ups: $5 for 50 credits (monthly plans), $60/year for 600 credits (annual plans). | https://www.productboard.com/pricing/ | 2026-10-14 |
| Trial | None mentioned | https://www.productboard.com/pricing/ | 2026-10-14 |
| Limits | Product Portals: Business 2, Enterprise included. Jira and Azure DevOps integrations: Business 5 each. | https://www.productboard.com/pricing/ | 2026-10-14 |
| Roadmaps | Included in both plans; release planning and multiple release groups listed | https://www.productboard.com/pricing/ | 2026-10-14 |
| Prioritization | Custom prioritization formulas included in both | https://www.productboard.com/pricing/ | 2026-10-14 |
| AI | Spark AI in both; AI Themes Enterprise only | https://www.productboard.com/pricing/ | 2026-10-14 |
| SSO | SAML SSO and Google SSO enforcement Enterprise only; Portal SSO in both | https://www.productboard.com/pricing/ | 2026-10-14 |
| API | API access in both | https://www.productboard.com/pricing/ | 2026-10-14 |
| Integrations named | Amplitude, Mixpanel, Jira, Azure DevOps, Salesforce, Gong, Figma, Miro, Mural, Slack, Microsoft Teams, Zendesk, Gainsight PX, Confluence, Google Drive, Notion, Chrome extension, MCP Server. Page says "25+ native integrations". | https://www.productboard.com/pricing/ | 2026-10-14 |

### Verified strengths
- Deep product-management feature set: custom prioritization formulas, objective hierarchy (Enterprise), release planning, AI with credits.
- Broad integrations incl. product analytics and sales-call tools.

### Honest trade-offs vs FIDMAP
- Productboard is a broader (and sales-led) PM suite; FIDMAP is narrower and self-serve. Per-maker with a 5-maker minimum implies a higher floor, but the actual dollar amount is UNVERIFIED so no price comparison can be made.
- Productboard's pricing page showed no public changelog, surveys or duplicate detection. "Not listed on one page" is not "not offered".

### UNVERIFIED
- Plan dollar prices; changelog, surveys, duplicate detection, webhooks (not listed); trial availability beyond the page.

---

## What we can safely say on comparison pages
(Each claim is verified from the fetched pages above on 2026-10-14. Re-check before publishing.)

1. FIDMAP shows its prices publicly. UserVoice and Productboard publish no plan prices on their pricing pages ("Talk to an Expert", "Request demo", "Contact sales"). Canny's Business tier is "Custom".
2. UserVoice states its pricing is usage-based and never by seat. Productboard prices per maker with a 5-maker minimum. Canny prices by tracked users. Frill lists flat monthly plans with unlimited teammates.
3. Frill's listed entry price is $25/mo (monthly) with a 14-day no-card trial. Canny Pro lists $79/mo billed yearly. Canny has a free plan limited to 25 tracked users.
4. UserVoice's trial is 30 days after booking a demo.
5. Canny's AI deduplication and summaries ("Autopilot") are stated as included in all plans.
6. Productboard lists SAML SSO as Enterprise-only.
7. Frill's Startup plan caps active ideas at 50.

## Claims to avoid
- Any statement of "cheaper than X" for UserVoice or Productboard (their prices are not public) or for Canny Business.
- "X has no changelog / roadmap / surveys / duplicate detection / AI / SSO / webhooks" for any competitor where we only found "not listed" or unmapped table cells.
- "Frill has no free tier" is supported by the page as fetched (none listed) but should be phrased "lists a 14-day trial and no free plan".
- Equating FIDMAP's "feedback posts" and "end users" with Canny's "tracked users" or Frill's "active ideas".
- Annual-price comparisons for Frill (annual prices not visible) and monthly-price comparisons for Canny Pro.
- Treating "$" as USD without verification.
- Any claim about competitors' workflow gaps (decision briefs, evidence linking) without testing their products directly.
- Figures from third-party blogs (not verified here).
