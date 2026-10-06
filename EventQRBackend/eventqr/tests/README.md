# Uncompiled tests

These 8 test files are kept outside `src/test/java`, so Maven does not compile or run them.
They were written against classes and methods that don't exist in this codebase, such as
`StaffService`, `StaffDtos`, `AdminDtos`, `ScanPurpose`, and Mockito's `anyUUID`. They have
never compiled.

To bring one back, rewrite it against the current API, move it into
`src/test/java`, and make sure `mvn test` passes. The working test suite is in
`src/test/java` and runs in CI (`.github/workflows/ci.yml`).
