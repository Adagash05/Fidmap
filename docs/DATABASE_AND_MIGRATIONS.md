# DATABASE AND MIGRATIONS

Status: **IMPLEMENTED—UNVERIFIED**. None of the SQL below was executed (no database in this sandbox, and the spec forbids running production migrations).

## How schema changes are applied today
The project uses Hibernate `ddl-auto: update` and has no Flyway/Liquibase. On first start of the new build, Hibernate **creates** the new tables and **adds** the new nullable columns automatically. It does not backfill data, drop anything, or add the unique constraints that would fail on dirty data. The SQL below is the explicit, idempotent equivalent for teams that want to apply it by hand before deploy (recommended for production) and the backfill that `update` cannot do. Take a backup first. Review every statement before running it.

## 1. New tables (idempotent)
```sql
CREATE TABLE IF NOT EXISTS feedback_followers (
  id UUID PRIMARY KEY,
  feedback_post_id UUID NOT NULL REFERENCES feedback_post(id),
  end_user_id UUID NOT NULL REFERENCES end_user(id),
  unsubscribe_token VARCHAR(64) NOT NULL UNIQUE,
  created_at TIMESTAMP NOT NULL,
  unsubscribed_at TIMESTAMP,
  CONSTRAINT uk_follower_post_user UNIQUE (feedback_post_id, end_user_id)
);
CREATE INDEX IF NOT EXISTS idx_follower_token ON feedback_followers(unsubscribe_token);

CREATE TABLE IF NOT EXISTS feedback_notifications (
  id UUID PRIMARY KEY,
  workspace_id UUID NOT NULL,
  feedback_post_id UUID NOT NULL,
  end_user_id UUID NOT NULL,
  event_key VARCHAR(120) NOT NULL,
  subject VARCHAR(300) NOT NULL,
  headline VARCHAR(300) NOT NULL,
  status VARCHAR(20) NOT NULL,
  attempts INTEGER NOT NULL DEFAULT 0,
  last_error VARCHAR(500),
  provider_message_id VARCHAR(120),
  created_at TIMESTAMP NOT NULL,
  next_attempt_at TIMESTAMP,
  sent_at TIMESTAMP,
  CONSTRAINT uk_notification_event UNIQUE (feedback_post_id, end_user_id, event_key)
);
CREATE INDEX IF NOT EXISTS idx_notification_status ON feedback_notifications(status, next_attempt_at);
CREATE INDEX IF NOT EXISTS idx_notification_workspace ON feedback_notifications(workspace_id, created_at);

CREATE TABLE IF NOT EXISTS feedback_priority_input (
  id UUID PRIMARY KEY,
  feedback_post_id UUID NOT NULL,
  workspace_id UUID NOT NULL,
  urgency INTEGER, strategic INTEGER, effort INTEGER,
  updated_by UUID, updated_at TIMESTAMP,
  CONSTRAINT uk_priority_input_post UNIQUE (feedback_post_id)
);

CREATE TABLE IF NOT EXISTS product_decision (
  id UUID PRIMARY KEY,
  feedback_post_id UUID NOT NULL REFERENCES feedback_post(id),
  workspace_id UUID NOT NULL,
  recommended_action VARCHAR(255) NOT NULL,
  decided_action VARCHAR(255) NOT NULL,
  rationale TEXT,
  score_snapshot INTEGER NOT NULL,
  confidence_snapshot VARCHAR(255),
  scoring_version VARCHAR(255),
  evidence_snapshot TEXT,
  roadmap_item_id UUID,
  decided_by UUID,
  decided_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS duplicate_suggestion (
  id UUID PRIMARY KEY,
  workspace_id UUID NOT NULL,
  source_post_id UUID NOT NULL,
  target_post_id UUID NOT NULL,
  similarity DOUBLE PRECISION NOT NULL,
  reason VARCHAR(500),
  status VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  resolved_by UUID, resolved_at TIMESTAMP,
  CONSTRAINT uk_dup_pair UNIQUE (source_post_id, target_post_id)
);

CREATE TABLE IF NOT EXISTS team_invitation (
  id UUID PRIMARY KEY,
  workspace_id UUID NOT NULL,
  email VARCHAR(255) NOT NULL,
  role VARCHAR(255) NOT NULL,
  token_hash VARCHAR(64) NOT NULL,
  status VARCHAR(255) NOT NULL,
  invited_by UUID,
  created_at TIMESTAMP NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  accepted_at TIMESTAMP,
  last_email_error VARCHAR(300)
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_invitation_token_hash ON team_invitation(token_hash);
CREATE INDEX IF NOT EXISTS idx_invitation_workspace ON team_invitation(workspace_id, status);

CREATE TABLE IF NOT EXISTS audit_log (
  id UUID PRIMARY KEY,
  workspace_id UUID,
  actor_id UUID,
  action VARCHAR(60) NOT NULL,
  target_type VARCHAR(40),
  target_id VARCHAR(64),
  detail VARCHAR(500),
  created_at TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_audit_workspace ON audit_log(workspace_id, created_at);
CREATE INDEX IF NOT EXISTS idx_audit_created ON audit_log(created_at);
```
Table names for `feedback_post`, `end_user` assume Spring's default snake_case naming; confirm with `\dt` before running. Enum columns are stored as strings (`EnumType.STRING`).

## 2. New columns on existing tables (all nullable, additive)
```sql
ALTER TABLE subscriptions   ADD COLUMN IF NOT EXISTS provider_modified_at TIMESTAMP;
ALTER TABLE feedback_post   ADD COLUMN IF NOT EXISTS merged_into_id UUID REFERENCES feedback_post(id);
ALTER TABLE feedback_post   ADD COLUMN IF NOT EXISTS merged_at TIMESTAMP;
ALTER TABLE votes           ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;
ALTER TABLE survey          ADD COLUMN IF NOT EXISTS closed_at TIMESTAMP;
ALTER TABLE survey          ADD COLUMN IF NOT EXISTS linked_feedback_post_id UUID REFERENCES feedback_post(id);
ALTER TABLE survey_question ADD COLUMN IF NOT EXISTS required BOOLEAN;  -- NULL is treated as not required
ALTER TABLE survey_response ADD COLUMN IF NOT EXISTS end_user_id UUID REFERENCES end_user(id);
```
`votes.created_at` is NULL for legacy rows; the recency component treats a missing vote date as unknown and falls back to the post's creation date (see `EvidenceCollector`).

## 3. Constraint that needs a data check first
Public survey responses are now one per end user. Before adding the unique constraint, check existing data:
```sql
SELECT survey_id, end_user_id, COUNT(*) FROM survey_response
WHERE end_user_id IS NOT NULL GROUP BY 1,2 HAVING COUNT(*) > 1;   -- must return 0 rows
ALTER TABLE survey_response
  ADD CONSTRAINT uk_survey_response_respondent UNIQUE (survey_id, end_user_id);
```
Hibernate `update` will try to add this constraint at startup; if duplicates exist it logs an error and continues. Existing rows have `end_user_id` NULL, and Postgres treats NULLs as distinct, so they do not conflict.

## 4. Required backfill: workspaces without a subscription row
Access is now evaluated server-side and **fails closed**: a workspace with no `subscriptions` row has no access (HTTP 402). Registration creates a trial row for new workspaces, but workspaces created before that behaviour may have none. Find them:
```sql
SELECT w.id, w.name FROM workspace w
LEFT JOIN subscriptions s ON s.workspace_id = w.id
WHERE s.id IS NULL;
```
Decide per workspace (grant a trial, mark as a paying customer from Polar data, or leave locked). **Do not bulk-grant access without a decision**; this is a business call, so no backfill INSERT is supplied. Confirm the exact `subscriptions` column list against your schema before writing one.

## 5. Rollback
All changes are additive. To roll back the application, deploy the previous build; the extra tables and columns are ignored by the old code. Drop them only after a backup and a decision that the data is not needed.

## 6. Not verified
Not run against Postgres. Column types for enums and timestamps follow Hibernate defaults and may differ slightly from what your existing schema reports (for example `timestamp(6)`). Compare with `ddl-auto: validate` in a staging copy before relying on this file.

## 7. Production-stabilization changes (survey limits, feedback_post columns)
Apply by hand before deploy if you do not want to rely on `ddl-auto: update`. All statements are idempotent. Back up first; none of this was executed here.
```sql
-- 7a. feedback_post: seven business-impact columns that no code reads.
-- A NOT NULL column without a default cannot be added to a populated table, so on a database that
-- already had rows Hibernate could not create them and every query that selects feedback_post
-- (feedback lists, and the roadmap through each item's linked requests) failed with HTTP 500.
ALTER TABLE feedback_post ADD COLUMN IF NOT EXISTS impact_score integer DEFAULT 0;
ALTER TABLE feedback_post ADD COLUMN IF NOT EXISTS paying_customer_count integer DEFAULT 0;
ALTER TABLE feedback_post ADD COLUMN IF NOT EXISTS affected_mrr numeric(15,2) DEFAULT 0;
ALTER TABLE feedback_post ADD COLUMN IF NOT EXISTS sales_opportunity_count integer DEFAULT 0;
ALTER TABLE feedback_post ADD COLUMN IF NOT EXISTS churn_risk_count integer DEFAULT 0;
ALTER TABLE feedback_post ADD COLUMN IF NOT EXISTS estimated_effort integer DEFAULT 3;
ALTER TABLE feedback_post ADD COLUMN IF NOT EXISTS demand_growth integer DEFAULT 0;
-- If the columns already exist as NOT NULL, relax them (the entity now tolerates NULL):
ALTER TABLE feedback_post ALTER COLUMN impact_score DROP NOT NULL;
ALTER TABLE feedback_post ALTER COLUMN paying_customer_count DROP NOT NULL;
ALTER TABLE feedback_post ALTER COLUMN affected_mrr DROP NOT NULL;
ALTER TABLE feedback_post ALTER COLUMN sales_opportunity_count DROP NOT NULL;
ALTER TABLE feedback_post ALTER COLUMN churn_risk_count DROP NOT NULL;
ALTER TABLE feedback_post ALTER COLUMN estimated_effort DROP NOT NULL;
ALTER TABLE feedback_post ALTER COLUMN demand_growth DROP NOT NULL;

-- 7b. Survey limits. NULL means unlimited, so existing rows MUST be backfilled.
-- (PlanDataInitializer also does this on every start.)
ALTER TABLE plans ADD COLUMN IF NOT EXISTS max_surveys integer;
UPDATE plans SET max_surveys = 1  WHERE billing_plan IN ('STARTUP_MONTHLY','STARTUP_YEARLY');
UPDATE plans SET max_surveys = 10 WHERE billing_plan IN ('BUSINESS_MONTHLY','BUSINESS_YEARLY','LIFETIME');
```
Lifetime follows the Business entitlements everywhere else in `PlanDataInitializer`, so its survey limit is 10. Downgrades never delete surveys: the workspace keeps them and cannot create more until it is within the limit.
