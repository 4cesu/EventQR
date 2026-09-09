package com.thedavelopers.eventqr.config;

public class TestConfig {
    // Appium server
    public static final String APPIUM_URL = "http://127.0.0.1:4723";

    // App
    public static final String APP_PACKAGE = "com.thedavelopers.eventqr";
    public static final String APP_ACTIVITY = ".features.landing.LandingActivity";
    public static final String AVD_NAME = "Medium_Phone_API_36";
    public static final String APK_PATH = "C:/Users/matth/Documents/EventQR/EventQRMobile/app/build/intermediates/apk/debug/app-debug.apk";

    // Test accounts (seeded in Supabase)
    public static final String ATTENDEE_EMAIL    = "attendee@gmail.com";
    public static final String ATTENDEE_PASS     = "Test123!";
    public static final String STAFF_EMAIL       = "staff@gmail.com";
    public static final String STAFF_PASS        = "Test123!";
    public static final String ORGANIZER_EMAIL   = "organizer@gmail.com";
    public static final String ORGANIZER_PASS    = "Test123!";
    public static final String ADMIN_EMAIL       = "admin@gmail.com";
    public static final String ADMIN_PASS        = "Test123!";
    public static final String SUPERADMIN_EMAIL  = "superadmin@gmail.com";
    public static final String SUPERADMIN_PASS   = "Test123!";
    public static final String NEW_USER_EMAIL    = "newuser_" + System.currentTimeMillis() + "@test.com";

    // Timeouts
    public static final int DEFAULT_WAIT_SECONDS = 5;
    public static final int LONG_WAIT_SECONDS    = 10;
}
