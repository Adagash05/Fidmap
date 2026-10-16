# DEPLOYMENT AND ENVIRONMENT

## Backend environment variables (from `application.yaml`)
| Variable | Purpose | New? |
|---|---|---|
| `DB_HOST` `DB_PORT` `DB_NAME` `DB_USERNAME` `DB_PASSWORD` | Postgres | existing |
| `PORT` | server port | existing |
| `JWT_SECRET_KEY` | JWT signing key (use a long random value) | existing |
| `GOOGLE_CLIENT_ID` `GOOGLE_CLIENT_SECRET` | Google sign-in | existing |
| `POLAR_API_URL` `POLAR_CLIENT_TOKEN` `POLAR_WEBHOOK_SECRET` | Polar billing | existing |
| `POLAR_STARTUP_MONTHLY_PRODUCT_ID` `POLAR_STARTUP_YEARLY_PRODUCT_ID` `POLAR_BUSINESS_MONTHLY_PRODUCT_ID` `POLAR_BUSINESS_YEARLY_PRODUCT_ID` `POLAR_LIFETIME_PRODUCT_ID` | Polar product ids (unchanged) | existing |
| `RESEND_API_KEY` | Resend | existing |
| `RESEND_FROM` | verified sender address | **new** |
| `FIDMAP_FRONTEND_URL` | links in emails, CORS | existing |
| `FIDMAP_ADMIN_BOOTSTRAP_TOKEN` | one-time token to create the first SUPER_ADMIN; **leave empty to disable** (default) | **new** |
| `FIDMAP_NOTIFICATION_INTERVAL_MS` | notification dispatch interval | **new** |

Also new: `server.forward-headers-strategy: framework` (client IP behind a proxy). Make sure only your trusted proxy can reach the app, otherwise `X-Forwarded-For` can be spoofed and the rate limiter bypassed.

No secrets are included in the ZIPs. Provide values through your host's secret store. The original archive contained a real `.env`; rotate those credentials.

## Frontend
Copy `.env.example` to `.env`: `VITE_API_BASE_URL` (backend URL), optional `VITE_WORKSPACE_ID`, `VITE_ROOT_DOMAIN`, `VITE_GA_MEASUREMENT_ID`. No new frontend variables were added.

## Deploy order
1. Back up the database.
2. Review and, if desired, apply DATABASE_AND_MIGRATIONS.md §1–3 by hand; otherwise Hibernate `update` creates them at startup.
3. Decide the §4 workspace backfill **before** release, or those workspaces will receive HTTP 402.
4. Verify the Resend sender domain; confirm the Polar webhook endpoint and secret.
5. Deploy backend, then frontend.
6. Create the first SUPER_ADMIN with the bootstrap token (`AuthenticationService.registerSuperAdmin`; it works only while no super-admin exists), then **clear the token and redeploy**.
7. Run ACCEPTANCE_TESTS.md.

## Build commands (not run in the authoring session)
Backend: `./mvnw -q test && ./mvnw -q package`. Frontend: `npm ci && npm run lint && npm run build`. Docker files under `Docker/` were not changed or tested.
