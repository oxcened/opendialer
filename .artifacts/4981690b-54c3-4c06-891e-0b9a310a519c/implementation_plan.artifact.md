# Fix Bottom Screen Overlap in Dialer

The dialer screen's footer (containing the dialpad and the Call button) overlaps with the system navigation bar. This is because the app is running in edge-to-edge mode, but the footer component does not account for the navigation bar insets.

## User Review Required

> [!NOTE]
> I will be updating the `targetSdkVersion` to 35 and `compileSdk` to 35 to comply with the latest edge-to-edge requirements. This is a recommended practice for modern Android apps.

## Proposed Changes

### Build Configuration

#### [MODIFY] [build.gradle (:app)](file:///Users/alenajam/sourcecodes/pokedialer-android/app/build.gradle)
- Update `compileSdk` to 35.
- Update `targetSdkVersion` to 35.

---

### UI Components

#### [MODIFY] [ContactsSearchScreen.kt](file:///Users/alenajam/sourcecodes/pokedialer-android/feature/contactsSearch/src/main/java/dev/alenajam/opendialer/feature/contactsSearch/ContactsSearchScreen.kt)
- Update the `Footer` composable to use `navigationBarsPadding()` or ensure it respects insets.
- Specifically, the `Surface` in `Footer` should handle `navigationBars` insets to avoid overlapping with the system navigation bar.

#### [MODIFY] [HomeScreen.kt](file:///Users/alenajam/sourcecodes/pokedialer-android/app/src/main/java/dev/alenajam/opendialer/ui/HomeScreen.kt)
- Ensure the `SearchBar` in the `topBar` accounts for the status bar insets using `statusBarsPadding()` or similar, as `Scaffold`'s `topBar` slot doesn't always automatically apply it to custom components like `SearchBar`.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to ensure the project builds with the new SDK target.

### Manual Verification
- Deploy the app to the emulator/device.
- Navigate to the Dialer (Contacts Search screen).
- Verify that the "Call" button is fully visible and not obscured by the system navigation bar.
- Verify that the status bar area in the Home screen is correctly handled.
