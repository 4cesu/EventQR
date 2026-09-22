# EventQR Android Client — Production Readiness Audit

**Scope:** `EventQRMobile` (module `:app`, package `com.thedavelopers.eventqr`) — XML Views + MV\* arch, Kotlin 2.2.10, AGP 9.2.1, minSdk 26 / targetSdk 35 / compileSdk 36.
**Date:** 2026-09-19
**Method:** Evidence-based static audit of ~198 Kotlin files (all core layers + every critical user flow). Confidence per finding: CONFIRMED (code path proven) / LIKELY (needs runtime or backend confirmation) / REQUIRES RUNTIME TESTING.
**Deliverable:** This document only — no source was modified.

---

## 1. Build / Release Configuration — VERDICT: NOT SHIPPABLE AS-IS

| # | Finding | Location | Severity |
|---|---------|----------|----------|
| B1 | **Release build has no signing config** — `assembleRelease` produces an unsigned APK (only the debug config installs). Play must receive an AAB; without release signing the artifact cannot even be installed locally. | `app/build.gradle.kts:26-32` | Critical |
| B2 | **R8/minification disabled** (`isMinifyEnabled = false`, no `shrinkResources`). Release ships with full debug symbols, no obfuscation (strings like base URLs, API shapes, and any secrets are trivially recoverable), and no dead-code removal (uCrop, zxing, Gson reflection paths all inflate size). | `app/build.gradle.kts:26-32`; `gradle.properties:24-25` | High |
| B3 | `versionCode = 1` / no `versionName` — fine for first release only; must gate Play distribution once set. | `app/build.gradle.kts:14` | Info |
| B4 | `androidx.security:security-crypto:1.1.0-alpha07` — **an alpha artifact in production**. Its storage format changed across alphas; upgrading later can silently invalidate stored credentials for installed users. Pin and test retention before wider rollout, or move to a stable Tink/Keystore-backed scheme. | `gradle/libs.versions.toml:19` | High |
| B5 | `proguard-rules.pro` is 21 lines of untouched template boilerplate — when R8 is enabled (B2), Gson reflective DTOs, zxing `QRCodeWriter`, uCrop, and the `InstantTypeAdapter` need rules proven by a release-build smoke test first. | `app/proguard-rules.pro` | High (latent) |
| B6 | `backup_rules.xml` and `data_extraction_rules.xml` are the AS-shipped samples containing TODO comments and empty rule sets. `allowBackup=false` (manifest) disables cloud backups, but on API 31+ device-to-device transfer is governed by `dataExtractionRules`; with no `<device-transfer>` exclusion the client can still be migrated with its token and any local files. Decide policy explicitly. | `res/xml/backup_rules.xml`, `res/xml/data_extraction_rules.xml`, `AndroidManifest.xml:14` | Medium |
| B7 | Third-party repo: `jitpack.io` in `settings.gradle.kts:22` (for uCrop). Supply-chain consideration — lock versions and pin. | `settings.gradle.kts:22` | Info |
| B8 | No analytics/crash reporting of any kind (no Sentry/Firebase/Crashlytics) — a production app has no error telemetry, making every crash below silent. | repo-wide | High |

---

## 2. Confirmed Bugs (code-level)

| # | Bug | Location | Confidence |
|---|-----|----------|-----------|
| C1 | **Password length message contradicts validation.** Login error says "Password must be at least 6 characters" but `Validators.isValidPassword` requires ≥8. Users with a 6–7 char password get a misleading message (8 might still fail — see C8). | `LoginPresenter.kt:38-40` vs `Validators.kt:18-20` | CONFIRMED |
| C2 | **"Check your email" shown on network failure.** Forgot-password shows the confirmation screen on `Error` too (offline/timeout → user told an email was sent). | `ForgotPasswordPresenter.kt:41-44` | CONFIRMED |
| C3 | **Duplicate/inconsistent `RegisterRequest` fields** — both `phone` and `phoneNumber` exist; the client only sets `phoneNumber` (`AuthRepository.createUser:43`). If the backend reads `phone`, registration silently drops the number. | `AuthDtos.kt:21-27` | LIKELY — verify backend |
| C4 | **`Unit as T` unchecked cast** — `safeApiCall` returns `Success(Unit as T, …)` when the server replies `success=true` with `data=null` (ApiCallHelper.kt:20). For any non-Unit endpoint where the backend returns success with a missing body (empty JSON, `{}`, gateway responses), this throws `ClassCastException` at runtime → crash. | `core/api/ApiCallHelper.kt:20` | LIKELY |
| C5 | **Raw `throwable.message` surfaced to UI** — "timeout", "Failed to connect to /10.0.2.2…", "Unable to resolve host…" shown verbatim in toasts across dozens of screens (`ApiCallHelper.extractMessage`, `DashboardRepository`, `StaffAssignedEventsActivity:133`, etc.). Only the admin event-requests screen maps messages to friendly text. | `core/api/ApiCallHelper.kt:32-42` et al. | CONFIRMED (UX bug) |
| C6 | **`RegisterRequest.phone`/`phoneNumber`** — see C3. | | |
| C7 | **`isRegisteredIn` uses string compare of `eventId.toString()` against raw param** — EventDetailPresenter.kt:81-87. If a caller passes an already-stringified UUID with different case, compare fails. Low impact; internal callers pass the same string normally. | `EventDetailPresenter.kt:83` | Low |
| C8 | Phone "editing" mangles non-Philippine numbers: register and edit-profile force a `+63` prefix and strip digits to 10 — a non-PH attendee cannot enter a correct number, and the UI silently alters input. | `RegistrationPresenter` (phone normalize), `AttendeeEditProfileActivity.kt:402-414` | CONFIRMED (by design, wrong for global users) |

---

## 3. Crash Risks

| # | Risk | Location | Confidence |
|---|------|----------|-----------|
| K1 | **QR "save to gallery" crashes on Android 8–9 (API 26–28).** Legacy path writes to public `Pictures/EventQR` via `FileOutputStream` with no `WRITE_EXTERNAL_STORAGE` runtime permission ever requested (only declared `maxSdkVersion=28`). On API 26–28 this throws `SecurityException`/`FileNotFoundException` (also `mkdir()` vs `mkdirs()`); the caller `AttendeeQrCredentialActivity:95-108` runs it inside `lifecycleScope.launch` without try/catch → **uncaught crash**. Same risk at the second call site `QrDisplayActivity`. | `core/util/BitmapSaver.kt:30-39`; `AttendeeQrCredentialActivity.kt:89-112`; `QrDisplayActivity` | CONFIRMED code path — REQUIRES RUNTIME TESTING on API 26–28 device |
| K2 | **Unguarded `UUID.fromString`** in the staff scan path: `ScannerPresenter.kt:64,66,112,115,123` and `RewardRedemptionScanResultActivity.kt:316-320` parse 4 IDs with no try/catch inside a coroutine. All values originate server-side, so risk is low — but one malformed/legacy value anywhere (migration, backend change) turns a scan into a crash instead of a graceful error. | `ScannerPresenter.kt`, `RewardRedemptionScanResultActivity.kt` | LIKELY (low probability, critical impact) |
| K3 | **Poster image decode without downsampling.** Event posters are fetched as files (`getStoredFile`, base64? — decode path in `EventDetailActivity`) and decoded at full size on the main thread; a large poster (or memory pressure) → `OutOfMemoryError`. No size/scale cap before `decodeByteArray`. | `EventDetailActivity.kt` (poster render section) | LIKELY |
| K4 | **`scanPurposeId` non-null UUID in `TransactionRequest`** built from a server-supplied `ScanPurposeResponse.scanPurposeId`; if the backend ever returns a non-UUID placeholder for a custom purpose, `UUID.fromString` crashes the scanner flow (same family as K2). | `ScannerPresenter.kt:67,116,124` | Low |
| K5 | **Coroutine leak → touch-after-destroy.** Fire-and-forget `MainScope().launch` without cancellation in: `StaffAssignedEventsActivity:124`, `OrganizerDashboardActivity:76-79`, `AdminEventApprovalActivity:145,322`, `EventDetailPresenter:61`, and the `dashboard`/`scanner` presenters that overwrite a single `job` without cancelling the previous one (rapid re-scan/orientation). Worst case: view callbacks against a destroyed activity + retained activity reference. | multiple (see locations) | CONFIRMED (leak) — crash only on edge paths |

---

## 4. Performance

| # | Finding | Location | Severity |
|---|---------|----------|----------|
| P1 | **Full-view-tree traversal on every activity create AND resume.** `AppBackButtonStyler` walks the entire window tree (via `root.post`) on `onActivityCreated` and `onActivityResumed` — O(n) on the main thread twice per screen, including for screens with no back-button styling need. | `EventQrApplication.kt` (AppBackButtonStyler) | Medium |
| P2 | **QR bitmap generation on the main thread** — `QRCodeWriter().encode(512×512)` + 262,144 `setPixel` calls per bitmap, at `AttendeeQrCredentialActivity.renderQrBitmap:143-153`, `QrDisplayActivity`, and per-card during batch **ID printing** (`AndroidIdPrinter.renderQrBitmap:509-519` × up to 9 cards/sheet). Print callbacks run on the app main thread → batch print of several sheets is a jank/ANR candidate. | `AttendeeQrCredentialActivity.kt`, `QrDisplayActivity.kt`, `AndroidIdPrinter.kt` | Medium |
| P3 | **No image library.** Every poster/file image is fetched + decoded manually, no caching/LRU, no downsampling (see K3), no disk cache — repeated list scrolling re-decodes. | repo-wide (`EventCardBinder`, `EventDetailActivity`, `ManageEvents/EditEventDetailsActivity`) | Medium |
| P4 | Cold start does a **network round-trip on the splash** (`refreshSessionAndNavigate`) with no timeout feedback; offline launch can sit on the splash up to the 10 s OkHttp read timeout before falling back to cached role. | `LandingActivity.kt:53-56, 114-134`; default OkHttp timeouts in `ApiClient.kt` | Medium |
| P5 | No connection pooling/HTTP version tuning, no HTTP/2 fast-fallback, no retry policy, 10 s default timeouts everywhere (incl. the free-tier Render backend whose container sleeps → cold starts of seconds). Combined: first scan of a session frequently exceeds the 10 s read timeout → "timeout" error → re-scan (2 s duplicate window guards UI, backend may already have logged the first transaction — see O3). | `core/api/ApiClient.kt` | Medium |

---

## 5. Offline / Error Handling

| # | Finding | Confidence |
|---|---------|-----------|
| O1 | **No offline mode.** Every screen hard-requires connectivity; errors are raw strings (C5). `RegistrationsCache` (30 s TTL, static singleton) is the only client-side cache and covers only registration status. | CONFIRMED |
| O2 | **No retry/backoff anywhere.** Single-shot requests; a flapping network mid-event loses the scan. | CONFIRMED |
| O3 | **Verify-then-commit is two network calls.** Staff scan → `verifyScan` → user taps "Log Transaction" → `createTransaction`. If connectivity drops between them, the attendee was verified but never logged; the staff member may re-scan, and the backend may see a duplicate. No idempotency key is sent with `TransactionRequest`. | CONFIRMED code path |
| O4 | **No request queue, no conflict resolution.** Multiple staff scanning the same attendee concurrently are arbitrated only by the backend (which may double-log without idempotency keys at the gate) — client shows the response as-is. | CONFIRMED |
| O5 | Empty-state views generally exist (chips + "no results"), but several screens show raw error text in the empty state and a toast (e.g. `StaffAssignedEventsActivity:133-136`). | CONFIRMED |

---

## 6. Auth / Session

| # | Finding | Location | Severity |
|---|---------|----------|----------|
| A1 | **`auth/logout` endpoint is never called.** All sign-out paths (`DashboardPresenter:148`, `AttendeeProfileActivity:107`, `StaffProfileActivity:84`) only clear local prefs. Server-side token revocation is dead code (`ApiService.logout:99-100` unreferenced, `refreshToken:102-103`). Tokens remain valid server-side after the user "signs out." | `ApiService.kt:99-103` (unused) | Medium |
| A2 | **No 401 handling / refresh-on-401.** `AuthInterceptor` adds the Bearer token and never reacts to 401 — an expired token leaves every subsequent request failing with raw "401" toasts and no re-auth path except manual re-login. | `core/api/AuthInterceptor.kt:11-18` | High |
| A3 | `refreshSessionToken()` is invoked fire-and-forget on dashboard resumes and once on landing — but nothing depends on its success for access (falls back to stale role silently, which is intentional for role-change UX but keeps stale tokens alive). | `LandingActivity.kt:121-132`, `DashboardActivity.kt:135`, `OrganizerDashboardActivity.kt:76-79`, `AuthRepository.kt:83-96` | Info |
| A4 | Credentials stored via `EncryptedSharedPreferences` (AES256_SIV/GCM) — good; but on the alpha library (B4) and with no automatic login/session expiry flow. | `core/session/SessionManager.kt` | Info |
| A5 | **Reset-password deep link on a third-party host without verification.** `ResetPasswordActivity` is `exported` with an app-link intent filter for `https://eventqr-backend-owoa.onrender.com/reset-password`. `autoVerify` requires `assetlinks.json` on that host — the host was unreachable during this audit (free-tier sleep), so verification state is UNKNOWN. If unverified, Android treats it as a browser deep link and Android 12+ may route it to the browser, breaking the reset flow; the token also travels in the query string (visible in browser history on that route). | `AndroidManifest.xml` (ResetPasswordActivity); host check failed (transport error) | REQUIRES RUNTIME TESTING |
| A6 | Camera permission requested on the pre-login landing screen (first launch) rather than at first scan — aggressive for a guest who may never scan. | `LandingActivity.kt:66-78` | UX Low |

---

## 7. UX Issues

| # | Finding | Notes |
|---|---------|-------|
| U1 | Stacks of toasts on partial dashboard failure (up to 3 raw-error toasts at once). | `DashboardPresenter.kt:127-135` |
| U2 | Dashboard reloads on every resume (staff: 2nd+ resume triggers full reload; attendee dashboard from `onResume`). Inconsistent with organizer (loads once). | `StaffDashboardActivity.kt:84-91`, `OrganizerDashboardActivity.kt:82-90` |
| U3 | `RegistrationPresenter` silently rewrites the phone input (prefix + truncation) without telling the user their number was changed. | `RegistrationPresenter` phone normalization |
| U4 | Attendee "View QR" screen exposes the raw QR value as plain text (debug/UX duplication); also `txtQrValue` shows it prominently. Minor. | `AttendeeQrCredentialActivity.kt:132` |
| U5 | Two parallel admin event-approval screens exist (`AdminEventApprovalActivity` = older "demo"-style with hardcoded colors and leftover "Showing local demo data" banner; `AdminEventApprovalBackendActivity` = modern). Dead code + design drift (hardcoded palette `#25215F`, `#5B25C9` vs DESIGN.md ink-black/`#2D2A7C`). | `features/admin/` |
| U6 | `index`/non-adaptive `setPixel` loops + no downsampling is also user-visible as stutter on event detail poster load. | see P2/P3 |
| U7 | Notification "mark all read" re-fetches the entire list (network + re-render). Minor. | `NotificationsPresenter.kt:46` |

---

## 8. Static Hunt Results

- **`!!`**: 4 total, all null-guarded date comparisons (`RequestEventActivity.kt:137,142,162`, `EventReportsActivity.kt:313`). No unguarded use. ✅
- **Empty catch blocks**: none found. ✅
- **`GlobalScope`/`viewModelScope`**: none (presenters use `MainScope()` — which is the leak source in §3/K5). ✅-ish
- **`Log.d/e/w` in production code**: 36 matches — debug logs left on in: `ManagerScanPurposesActivity` ("persistenceTag" verbose logging per toggle/save), `ScannerPresenter/Activity` ("StaffQrScanner"), `StaffScanResultActivity`, `IdPrinter`, `TransactionRulesActivity`, `EventStatusBadgeStyler` (`Log.w` on unknown status — acceptable), `EventDetailActivity:407` (`Log.d` of registration window in main source), `OrganizerDashboardActivity` (`Log.w` on load failure — acceptable). Remove/guard behind `BuildConfig.DEBUG`.
- **Hardcoded URLs**: only the default `BASE_URL` build-config (overridable) + assetlinks reference. ✅
- **Deprecated APIs**: legacy `android.hardware.Camera` with `@Suppress("DEPRECATION")` in `ScannerActivity` (8 decode attempts/frame; works, but no autofocus control, no CameraX, no multi-camera handling — flag for medium-term replacement; performance/UX risk at the gate).

---

## 9. Test Coverage Gap

- Only `ExampleUnitTest`, `PortalSwitcherTest` (real), `ExampleInstrumentedTest`. **Zero tests for presenters/repositories/critical flows.**
- No UI/E2E tests; no Detox/Maestro. The riskiest surfaces (QR save on API 26–28, deep-link reset on 12+, reward redemption grant, batch ID printing, scanner decode on low-end devices) have no automated coverage.
- Recommended before launch (even manual QA can be scripted): 
  1. API 26/27/28 device: save QR to gallery (K1) 
  2. API 31+: reset-password deep link from an installed-tap context (A5) 
  3. Offline scan + re-scan (O3) 
  4. Low-end device: event poster list + QR render + batch print 3-sheet (P1-P3) 
  5. Release build with R8 ON (B2/B5) smoke test of login/QR/print.

---

## 10. Consolidated Severity Table

| Sev | # | Short |
|-----|---|-------|
| **Critical** | B1 | Release APK unsigned |
| **High** | B2 | R8/shrink off |
| **High** | B4 | security-crypto **alpha** in prod |
| **High** | B8 | No crash/analytics telemetry |
| **High** | A2 | No 401 handling/refresh |
| **High** | C4 / K1 | `Unit as T` cast crash / QR save crash API 26-28 |
| **Medium** | B6, C2, C5, K2, K3, K5, P1, P2, P3, P4, P5, O3, A1, A5, U1 | see rows |
| **Low/Info** | C1, C7, C8, U2-U7, logs, jitpack, versionCode | see rows |

---

## Suggested Fix Order (dependencies first)

1. **Release signing + R8 + shrinkResources ON** with a keep-rules pass (B1/B2/B5) → then re-run full smoke.
2. **Remove alpha crypto** or embed a data-migration test for encrypted prefs (B4).
3. **safeApiCall success-with-null guard** — return `Error` instead of the `Unit as T` cast (C4).
4. **BitmapSaver legacy path**: use MediaStore on all API levels (drop the legacy branch) or request permission properly (K1).
5. **401 refresh interceptor** + actually call `auth/logout` on sign-out (A1/A2).
6. **Guard all `UUID.fromString`** with runCatching in scan paths (K2/K4).
7. **Move QR bitmap + decode work off the main thread** (P2/P3, K3).
8. Friendly error mapping helper shared across screens (C5) and remove debug logs in release (Log.d).
9. Add crash reporting (B8) before any real rollout.