# EventQR Appium UI Test Suite

Appium 2.x UI automation for the EventQR Android app.
Tech: Appium 2.x, Java 21, JUnit 5, Maven. Target: Android Emulator only.

## Prerequisites

- **Node.js** (for Appium)
- **Appium 2.x** CLI
- **uiautomator2 driver** for Appium
- **Java 21** JDK
- **Maven** (3.8+)
- **Android Studio** with an AVD configured (emulator)

## Install Commands

```bash
# Install Appium globally
npm install -g appium

# Install the uiautomator2 driver
appium driver install uiautomator2
```

## Config Steps

Before running, edit `src/test/java/com/thedavelopers/eventqr/config/TestConfig.java`:

1. `APP_ACTIVITY` — verify against the real AndroidManifest (currently `.LandingActivity`)
2. `AVD_NAME` — set to your actual emulator AVD name
3. `APK_PATH` — absolute path to your compiled `eventqr.apk`
4. Test account credentials — replace with real seeded test accounts
   (`ATTENDEE_EMAIL`, `STAFF_EMAIL`, `ORGANIZER_EMAIL`, `ADMIN_EMAIL`, `SUPERADMIN_EMAIL`)

## Run Commands

Start Appium in one terminal:

```bash
appium
```

Run the tests in a second terminal (from project root):

```bash
mvn test
```

Run a single test class:

```bash
mvn test -Dtest=LoginTest
```

## Finding Element Resource-IDs

Many locators are marked with `// TODO: verify resource-id in Appium Inspector`.
These must be confirmed against the actual APK before running. Use either:

- **Appium Inspector** (recommended for Appium): launch server, connect to the
  session, inspect the screen hierarchy
- **Android Studio Layout Inspector**: while the app runs on the emulator, open
  `Layout Inspector` and copy `resource-id`s from the view tree

## Self-Service Runbook (this machine)

One command starts everything (emulator + Appium with `ANDROID_HOME` set) and runs the suite:

```powershell
# from eventqr-appium-tests\
.\run-tests.ps1                 # full suite
.\run-tests.ps1 -Test LoginTest # single class: ".\run-tests.ps1 -Test LoginTest"
```

Inspect the current screen's real resource-ids while the app runs on the emulator:

```powershell
.\dump-ui.ps1
```

Fixes vs. original scaffold (verified against the live app):
- Fresh app state per test (`noReset=false`) + `autoGrantPermissions(true)` so sessions never leak
- `setUp()` auto-navigates Landing -> Sign In before each test
- `isDisplayed`/`isTextDisplayed` now wait (up to `DEFAULT_WAIT_SECONDS`) instead of instant poll — fixes dashboard-login race conditions
- LoginPage: forgot-password link id `txtForgotPassword`; RegistrationPage ids `edtFirstName`/`edtLastName`/`edtPhoneNumber`/`btnRegister`
- Appium must run with `ANDROID_HOME=C:\Users\matth\AppData\Local\Android\Sdk` (script does this)
- Verified `APP_ACTIVITY=.features.landing.LandingActivity` (matches AndroidManifest launcher)

Still TODO (will fail until corrected — none are crash/harness bugs):
- Attendee/Staff/Organizer dashboard assertions use `txtWelcome` (unverified). Dump the real dashboard id with `.\dump-ui.ps1` after logging in and update `AttendeeDashboardPage`/`StaffDashboardPage`/`OrganizerDashboardPage`.
- Field-error assertions (`txtEmailError`, `txtPasswordError`, `tilEmail`, `tilPassword`) and the login loading indicator (`progressLogin`/text "Loading") are unverified.
- Series of per-role test ids across attendee/staff/organizer/admin tests remain TODOs (marked in code).

Test accounts (Supabase, password `Test123!`): attendee@/staff@/organizer@/admin@/superadmin@gmail.com.

## Important Notes

- All `TODO` resource-ids must be verified against the actual APK before running
- Resource-ids are auto-prefixed with the app package via the `id()` helper
- Tests use explicit waits only — no `Thread.sleep()`
- Camera-dependent scan tests are excluded from scope (`@Disabled("Requires camera — excluded from scope")`)
- E2E cross-role flows are `@Disabled` stubs requiring seeded data
- Tests are independently runnable (no inter-test state dependencies)

## Project Layout

```
src/test/java/com/thedavelopers/eventqr/
├── base/BaseTest.java          # driver setup + helper methods
├── config/TestConfig.java       # credentials + settings
├── pages/                       # Page Object Model
│   ├── LoginPage.java
│   ├── RegistrationPage.java
│   ├── AttendeeDashboardPage.java
│   ├── StaffDashboardPage.java
│   ├── OrganizerDashboardPage.java
│   └── AdminDashboardPage.java
└── tests/
    ├── auth/                    # login, registration, forgot, change password
    ├── attendee/                # dashboard, events, registered, rewards, profile
    ├── staff/                   # dashboard, logs
    ├── organizer/               # dashboard, event mgmt, reports
    ├── admin/                   # dashboard, event approval, account mgmt
    ├── navigation/              # portal switcher, role switching
    └── e2e/                     # cross-role E2E stubs
```
