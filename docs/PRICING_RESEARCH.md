# FIDMAP Pricing Research

Research date: 2026-10-14. Sources: official pricing pages fetched via WebFetch (summaries; see COMPETITOR_RESEARCH.md for caveats). All prices shown as "$" on the pages; currency code UNVERIFIED.

## 1. Verified competitor price points

| Competitor | Entry | Mid | Upper | Model | Source |
|---|---|---|---|---|---|
| Canny | Free: $0, 25 tracked users, 5 managers | Pro: $79/mo billed yearly (100+ tracked users, 10 managers). Plain monthly price not shown. | Business: Custom (5,000+ tracked users), contact sales | Tracked-user tiers; unlimited contributors | https://canny.io/pricing |
| Frill | Startup: $25/mo (50 active ideas, 1 survey) | Business $49/mo (unlimited ideas, 3 surveys); Growth $149/mo | Enterprise: $349/mo shown, "Talk to sales" | Flat monthly plans; unlimited admins and tracked users | https://frill.co/pricing |
| UserVoice | Not public | Not public | Not public (contact sales) | Usage-based (feedback volume, tools, integrations), never per seat | https://www.uservoice.com/pricing |
| Productboard | Not public (Business, "Request demo") | Not public | Enterprise: contact sales | Per maker, 5-maker minimum; AI credits per maker; annual saves 21% | https://www.productboard.com/pricing/ |

Notes:
- Frill annual prices: UNVERIFIED (toggle exists, figures not seen).
- Productboard credit top-ups are the only dollar amounts shown ($5 per 50 credits monthly; $60/year per 600 credits annual).
- Trials: Frill 14 days no card; UserVoice 30 days after demo; Canny trial exists on Pro, length not stated; Productboard none mentioned.
- No lifetime/one-time deals were seen on any of the four pricing pages (UNVERIFIED beyond what was fetched).

## 2. Observed pricing models
- Per tracked user (Canny): scales with the customer's audience; can create surprise tier jumps (auto-upgrade).
- Flat tiers with unlimited seats (Frill): simple, cheap, differentiates on feature caps (ideas, surveys).
- Usage-based, sales-led (UserVoice) and per-maker, sales-led (Productboard): opaque, enterprise-oriented.
- FIDMAP's current structure (tiers capped by team members, boards, posts, end users) is closest to Canny/Frill hybrids.

## 3. PROPOSAL (not confirmed configuration)

The actual USD prices live in Polar and are unknown to this research. The numbers below are a proposal only.

### Assumptions (explicit)
- FIDMAP's cost to serve (hosting, AI inference, email, payment fees, support) is UNKNOWN. No margin figure is claimed. Before adopting any price, compute cost per active workspace, especially any AI/LLM usage per brief, and confirm gross margin target.
- Payment processor/merchant-of-record fees are unknown and assumed to take a single-digit to low-double-digit percentage; verify with Polar.
- Competitor public anchors: Frill $25 / $49 / $149 monthly; Canny Pro $79/mo billed yearly. FIDMAP's Business (10 seats, 10,000 posts, 25,000 users, private boards, branding removal) is functionally comparable to a Frill Business-to-Growth or Canny Pro-class offer.
- FIDMAP's differentiation (evidence-backed decision briefs, connected feedback-to-roadmap-to-changelog workflow) is a positioning claim, not verified as superior.

### Proposed price points

| Plan | Monthly | Yearly | Rationale |
|---|---|---|---|
| Startup | $29 | $290 (about 2 months free) | Slightly above Frill Startup ($25) but with far higher post cap (1,000 vs 50 active ideas, not identical units) and 3 seats; well below Canny Pro ($79). Entry price needs to be low enough to convert a 7-day trial. |
| Business | $79 | $790 (about 2 months free) | Aligns with Canny Pro's $79/mo billed yearly and sits between Frill Business ($49) and Growth ($149). Value: 10 seats, 10,000 posts, 25,000 end users, private boards, branding removal. Monthly billing at the same level as Canny's annual-billed price is a deliberate premium for decision-brief value; if conversion is weak, test $59 to $69. |
| Lifetime | $1,190 one-time (about 1.5x Business yearly) | n/a | See risks below. Proposed with a hard cap on number of sales and clear "fair use" limits equal to Business. |

Price-test suggestion: treat these as hypotheses, A/B test Startup at $29 vs $39 and Business at $69 vs $79 via Polar before committing.

### Rationale
- Value delivered: FIDMAP replaces a feedback board, roadmap and changelog, plus produces decision briefs. Priced at parity with Canny Pro for Business because the quantified comparison favors FIDMAP on user/post limits only partially (Canny posts are unlimited per the page, so do not claim limit superiority).
- Competitor range: verified public range is $25 to $149 monthly for self-serve tiers; Business at $79 sits in the middle.
- Sustainable margins: UNKNOWN until FIDMAP costs are measured. AI features in particular have variable cost; Productboard meters AI via credits, a possible model if FIDMAP's AI costs prove material.
- Annual discount: about 17% (2 months free) is below Productboard's stated 21% and Canny's "up to 20%", so is in line with the market.

### Lifetime-deal risks
- Perpetual obligation against recurring costs (hosting, AI, support) with one payment; cash-positive early but negative long term if usage is heavy.
- Cannibalizes Business yearly renewals; at 1.5x yearly price, a rational customer chooses Lifetime. A higher multiple (2.5x to 3x, about $1,990 to $2,370) reduces cannibalization but hurts conversion. Choose the multiple after estimating customer lifetime and cost to serve.
- Feature creep expectation: define whether Lifetime includes future AI features and add-ons. Recommend "Business plan limits as of purchase, fair use, no AI credit guarantee".
- Cap the quantity or time window; consider ending the offer after a launch period.
- None of the four competitors showed a lifetime plan on the fetched pages, so there is no market anchor.

### Migration risk for existing customers
- Do not change prices for current subscribers without notice. Grandfather existing Polar subscriptions at their current price where possible; moving customers between Polar products/prices can trigger re-subscription or proration issues (verify Polar behavior).
- If limits change (e.g., reducing caps), apply only to new signups or give notice, to avoid existing workspaces exceeding limits.
- Existing Lifetime holders: never reduce their entitlements; changing Lifetime price only affects new buyers.
- Because current Polar prices are unknown, first read the actual Polar product prices and compare with this proposal. If current prices are already lower, raise only for new customers.
- Communicate any change at least one billing cycle in advance and keep plan identifiers in code (monthly/yearly Startup/Business, Lifetime) stable so entitlements map correctly.

## 4. Open questions needing verification
- Actual current Polar prices for all FIDMAP plans.
- FIDMAP cost to serve per workspace and AI cost per brief.
- Frill annual prices; Canny Pro monthly price; UserVoice and Productboard actual prices (ask sales or request quotes).
- Whether FIDMAP's "end users" metric is comparable to Canny "tracked users".
