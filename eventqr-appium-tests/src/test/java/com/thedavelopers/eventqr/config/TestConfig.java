package com.thedavelopers.eventqr.config;

public class TestConfig {
    // Appium server
    public static final String APPIUM_URL = "http://127.0.0.1:4723";

    // App
    public static final String APP_PACKAGE = "com.thedavelopers.eventqr";
    public static final String APP_ACTIVITY = ".LandingActivity"; // TODO: verify against AndroidManifest
    public static final String AVD_NAME = "YOUR_AVD_NAME";        // replace with actual AVD name
    public static final String APK_PATH = "/absolute/path/to/eventqr.apk"; // replace

    // Test accounts (replace with real seeded test credentials)
    public static final String ATTENDEE_EMAIL    = "attendee@test.com";
    public static final String ATTENDEE_PASS     = "Test@1234";
    public static final String STAFF_EMAIL       = "staff@test.com";
    public static final String STAFF_PASS        = "Test@1234";
    public static final String ORGANIZER_EMAIL   = "organizer@test.com";
    public static final String ORGANIZER_PASS    = "Test@1234";
    public static final String ADMIN_EMAIL       = "admin@test.com";
    public static final String ADMIN_PASS        = "Test@1234";
    public static final String SUPERADMIN_EMAIL  = "superadmin@test.com";
    public static final String SUPERADMIN_PASS   = "Test@1234";
    public static final String NEW_USER_EMAIL    = "newuser_" + System.currentTimeMillis() + "@test.com";

    // Timeouts
    public static final int DEFAULT_WAIT_SECONDS = 5;
    public static final int LONG_WAIT_SECONDS    = 10;
}
