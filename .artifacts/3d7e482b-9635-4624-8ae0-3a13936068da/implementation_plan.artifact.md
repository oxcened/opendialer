# Fix CI Build Failure (Lint Analysis Crash)

The build is failing in GitHub Actions during lint analysis because the project is using a very recent version of Android Gradle Plugin (9.3.1) and Gradle (9.7.0), which require JDK 21 to run correctly. The current CI configuration uses JDK 17, causing Lint to crash when it attempts to call methods introduced in Java 21 (like `List.removeLast()`).

## Proposed Changes

### CI Configuration

#### [MODIFY] [android.yml](file:///Users/alenajam/sourcecodes/pokedialer-android/.github/workflows/android.yml)
Update the JDK version from `17` to `21`.

### Build Configuration

#### [MODIFY] All `build.gradle.kts` files
Update `jvmToolchain(17)` to `jvmToolchain(21)` to align the compilation target with the build environment requirements. This also includes fixing a duplicate `jvmToolchain` call in `:core:aosp`.

## Verification Plan

### Automated Tests
- The primary verification will be the successful run of the GitHub Actions workflow.
- Locally, I will run `./gradlew lintAnalyzeDebug` to ensure it still works with the updated toolchain settings (assuming local JDK is 21).

### Manual Verification
- Verify that the build completes successfully on the developer machine.
