# Implementation Plan: Issues #104–#108 (M3 Anti-Abuse + M4 Discovery)

**Branch:** `feature/issues-104-108-m3-m4-investigation`
**Based on:** `feature/issues-98-103-m2-implementation`
**Date:** 2026-04-17

---

## Issue Overview

| # | Title | Milestone | Implementation | Tests |
|---|-------|-----------|----------------|-------|
| #104 | Suspicious behavior detection | M3 | ✅ Complete | ❌ Missing |
| #105 | Risk scoring | M3 | ✅ Complete | ❌ Missing |
| #106 | Step-up verification gates | M3 | ✅ Complete | ❌ Missing |
| #107 | Artist/performer entity | M4 | ✅ Complete | ❌ Missing |
| #108 | Events near you / geolocation | M4 | ✅ Complete | ❌ Missing |

**All implementation is complete. The only remaining work is tests.**

---

## What Is Implemented

### Backend — new modules and files

```
fraud/
  api/
    FraudAdminController.java       — GET /admin/fraud/flags, GET /admin/fraud/flags/{userId},
                                       PATCH /admin/fraud/flags/{flagId}/resolve,
                                       GET /admin/fraud/risk/{userId}
    StepUpController.java           — POST /auth/step-up/verify
    StepUpFilter.java               — OncePerRequestFilter; guards POST /api/payments/checkout
                                       and POST /api/events/*/holds with HTTP 428
  application/
    BehaviorAnalysisService.java    — queries AuditLog for hold cycling, release rate,
                                       volume anomaly, failed payment patterns
    BehaviorAnalysisSweepScheduler.java — @Scheduled sweep (fraud.analysis.interval-ms: 300000)
    RiskScoringService.java         — additive score (0–100), tier mapping, nightly decay job
    StepUpGateService.java          — requiresStepUp(), markVerified(), isVerified()
                                       Redis key: step_up_verified:{userId}, RBucket<Boolean>, TTL 15 min
    StepUpRequiredException.java    — carries action field; mapped to HTTP 428 in GlobalExceptionHandler
    SuspiciousFlagService.java      — creates/resolves flags, dedup window, triggers score recalc
                                       exposes hasActiveCriticalFlag(userId) for QueueService
    UserFlaggedForAbuseException.java — thrown by QueueService.joinQueue() for CRITICAL-flagged users
  domain/
    RiskScore.java, RiskTier.java (enum), SuspiciousFlag.java,
    SuspiciousFlagSeverity.java (enum), SuspiciousFlagType.java (enum)
  infrastructure/
    RiskScoreRepository.java, SuspiciousFlagRepository.java

performers/
  api/    PerformerController.java  — GET /performers, GET /performers/{id},
                                       POST /performers (admin), PUT /performers/{id} (admin)
  application/ PerformerService.java — CRUD, case-insensitive dedup check
  domain/  Performer.java
  infrastructure/ PerformerRepository.java — findByNameIgnoreCase(), existsByNameIgnoreCaseAndIdNot()
  dto/    PerformerResponse.java, CreatePerformerRequest.java, UpdatePerformerRequest.java
```

### Backend — modified files

| File | Change |
|------|--------|
| `audit/infrastructure/AuditLogRepository.java` | Added `findByUserIdAndCreatedAtAfter`, `countByUserIdAndActionAndCreatedAtAfter`, `countByUserIdAndActionAndResourceTypeAndCreatedAtAfter`, `findDistinctUserIdsByCreatedAtAfter` |
| `config/GlobalExceptionHandler.java` | Added HTTP 428 handler for `StepUpRequiredException` |
| `config/SecurityConfig.java` | Updated (existing `/api/admin/**` rule already covers fraud admin endpoints) |
| `events/api/EventController.java` | Added `performerName` query param; wired `/api/events/nearby` endpoint using `GeoSearchService` |
| `events/application/EventService.java` | Extended `search()` Specification to filter by performer name via LEFT JOIN |
| `events/domain/Event.java` | Added `@ManyToMany performers` via `event_performers` join table |
| `events/dto/EventResponse.java` | Added `List<PerformerResponse> performers` field and factory mapping |
| `queue/application/QueueService.java` | `joinQueue()` calls `suspiciousFlagService.hasActiveCriticalFlag(userId)` and throws `UserFlaggedForAbuseException` |
| `venues/domain/Venue.java` | Added `latitude`, `longitude` fields (`DECIMAL(9,6)`) |
| `venues/infrastructure/VenueRepository.java` | Added `findVenuesWithinRadius()` via `@Query(nativeQuery = true)` Haversine formula |
| `venues/application/VenueService.java` | Updated to handle lat/lon in create/update |
| `venues/dto/CreateVenueRequest.java`, `UpdateVenueRequest.java`, `VenueResponse.java` | Added lat/lon fields |

### Backend — new files (venues module)

| File | Description |
|------|-------------|
| `venues/application/GeoSearchService.java` | Interface: `findEventsNear(lat, lon, radiusKm, pageable)` |
| `venues/application/GeoSearchServiceImpl.java` | Haversine via VenueRepository → filters PUBLISHED/ACTIVE events → sorted by distance |
| `venues/infrastructure/VenueDistance.java` | Projection: `venueId`, `distanceKm` |
| `events/dto/NearbyEventResponse.java` | DTO wrapping EventResponse + `distanceKm` field |

### Database migrations

| Migration | Contents |
|-----------|----------|
| `V22__suspicious_behavior.sql` | `suspicious_flags` table (TEXT details, not JSONB); composite index `idx_audit_user_created(user_id, created_at)` on `audit_logs` |
| `V23__risk_scores.sql` | `users.created_at` column (IF NOT EXISTS); `user_risk_scores` table |
| `V24__performers.sql` | `performers` table; `event_performers` junction table |
| `V25__venue_coordinates.sql` | `venues.latitude`, `venues.longitude` (`DECIMAL(9,6)`); partial index on non-null rows |

### Frontend — modified files

| File | Change |
|------|--------|
| `api/client.js` | 428 handled explicitly: dispatches `auth:step-up-required` custom event with action payload; does NOT trigger token refresh |
| `pages/Checkout.js` | Step-up CAPTCHA modal: listens for `auth:step-up-required`, renders inline Recaptcha, calls `POST /auth/step-up/verify`, retries original order POST |
| `pages/Events.js` | Added performer name text filter (`performerName` query param); added Near Me toggle (browser `Geolocation API` → `/api/events/nearby`); displays distance on cards |
| `pages/EventDetail.js` | Displays performer list ("Featuring: name (genre), …") |
| `admin/pages/AdminVenuesPage.js` | Added optional Latitude / Longitude number fields (type=number, min/max validation, WGS84 helper text) |

### Configuration (application.properties)

```properties
# Fraud Detection
fraud.analysis.interval-ms=300000
fraud.flag.dedup-window-minutes=30
fraud.rules.rapid-hold-count=5
fraud.rules.rapid-hold-window-minutes=2
fraud.rules.volume-anomaly-count=10
fraud.rules.volume-anomaly-window-minutes=10
fraud.rules.high-release-rate-threshold=0.8
fraud.rules.high-release-rate-window-minutes=60
fraud.rules.failed-payment-count=3
fraud.rules.failed-payment-window-hours=24

# Risk Scoring
fraud.score.low-severity-points=5
fraud.score.medium-severity-points=15
fraud.score.high-severity-points=30
fraud.score.payment-failure-points=20
fraud.score.new-account-points=10
fraud.score.payment-failure-threshold=3
fraud.score.new-account-hours=24
fraud.score.decay-cron=0 0 2 * * *

# Step-Up Verification
fraud.stepup.verified-ttl-minutes=15
fraud.stepup.high-tier-checkout-enabled=true
fraud.stepup.critical-any-action-enabled=true
fraud.stepup.rapid-hold-cycling-enabled=true

# Geolocation
geo.nearby.default-radius-km=50
geo.nearby.max-radius-km=500
```

---

## Remaining Work — Tests Only

No test directories exist for `fraud/`, `performers/`, or `venues/` in the test tree. No frontend tests cover any of the new features. Every item below is missing.

### Backend tests to write

**fraud module** — new directory: `backend/src/test/java/com/fairtix/fraud/`

| Class to test | Key cases |
|---------------|-----------|
| `BehaviorAnalysisService` | Each rule fires at threshold (rapid hold: >5 in 2 min, volume: >10 in 10 min, release rate: >80%, failed payments: >3); rules do NOT fire below threshold; dedup window suppresses duplicate flags |
| `RiskScoringService` | Score accumulates correctly per severity weight; tier boundaries (0–24 LOW, 25–49 MEDIUM, 50–74 HIGH, 75–100 CRITICAL); score capped at 100; decay reduces score on schedule |
| `StepUpGateService` | `requiresStepUp()` returns true for HIGH tier at checkout, CRITICAL tier for any action, `RAPID_HOLD_CYCLING` flag at seat hold; returns false for LOW/MEDIUM tier; `markVerified()` / `isVerified()` round-trip |
| `SuspiciousFlagService` | Flag created; `hasActiveCriticalFlag()` returns true only for unresolved HIGH/CRITICAL flags |

**performers module** — new directory: `backend/src/test/java/com/fairtix/performers/`

| Class to test | Key cases |
|---------------|-----------|
| `PerformerService` | Create succeeds; duplicate name (case-insensitive) is blocked; update works; list is paginated |

**EventServiceTest** — extend existing file at `backend/src/test/java/com/fairtix/event/application/EventServiceTest.java`

| Test | What to verify |
|------|----------------|
| `searchingByPerformerNameReturnsMatchingEvents` | Events linked to a performer matching the name are returned; events without a matching performer are excluded |

**venues / GeoSearchService** — new directory or extend existing: `backend/src/test/java/com/fairtix/venues/`

| Class to test | Key cases |
|---------------|-----------|
| `GeoSearchServiceImpl` | Events within radius returned sorted by distance; events outside radius excluded; only PUBLISHED/ACTIVE events included; max radius enforced |

> **Test compatibility note:** `GeoSearchService` is behind an interface — mock the interface in unit tests. `VenueRepository`'s Haversine native query uses PostgreSQL functions and will fail on H2; do not test the query method directly with H2 — it is covered by the service-level mock.

### Frontend tests to write

**`api/client.test.js`** — add:
- 428 response dispatches `auth:step-up-required` event with correct `action` from response body
- 428 does NOT trigger the token refresh loop (401/403 path)

**`pages/Checkout.test.js`** — add:
- Submitting payment when backend returns 428 shows step-up CAPTCHA modal
- Submitting valid CAPTCHA token calls `POST /auth/step-up/verify`
- After verification, original order POST is retried
- Failed CAPTCHA shows inline error

**`pages/Events.test.js`** — add:
- Typing in performer filter sends `performerName` query param to API
- Near Me toggle calls `navigator.geolocation.getCurrentPosition`
- Successful geolocation fetches from `/api/events/nearby` with lat/lon params
- Denying geolocation hides or disables the Near Me toggle (graceful degrade)
- Nearby results display distance label on cards

**`pages/EventDetail.test.js`** — add:
- Event with performers renders "Featuring:" section with name and genre
- Event without performers renders no "Featuring:" section

---

## Notes and Known Issues

**StepUpFilter vs GlobalExceptionHandler — minor redundancy**
`StepUpFilter` writes the 428 response directly to `HttpServletResponse` (before the request reaches the controller). `GlobalExceptionHandler` also maps `StepUpRequiredException` to 428. In normal flow the filter intercepts first, so the exception handler is only triggered if a controller manually throws `StepUpRequiredException`. Both are harmless to leave in place; no action required unless it causes test confusion.

---

## Open Questions

1. **#106 Step-up challenge type:** Is Google reCAPTCHA v2 sufficient for M3, or does the team want TOTP/email OTP for a future milestone?
2. **#107 Performer ownership:** Currently admin-only create/update — confirm this is acceptable or open to organizers.
3. **#108 Manual location fallback:** If user denies geolocation, should the frontend offer a city/zip text input, or is hiding the Near Me toggle sufficient?
