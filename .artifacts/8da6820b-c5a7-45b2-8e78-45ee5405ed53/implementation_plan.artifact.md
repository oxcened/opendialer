# Implementation Plan: Phase 2 - ViewModel Purity

This plan covers refactoring the `InCallViewModel` to be "pure" by removing dependencies on Android resources and string formatting, moving that responsibility to the UI layer.

## Proposed Changes

### [:feature:inCall](file:///Users/alen/StudioProjects/opendialer/feature/inCall)

#### [MODIFY] [InCallUiState.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/ui/InCallUiState.kt)
- Define `CallStatus` enum to represent different call states (Ringing, Active, etc.).
- Update `InCallUiState` to use `CallStatus` instead of `stateLabel: String`.
- Update `ConferenceParticipantUiState` to use `CallStatus`.

#### [MODIFY] [InCallViewModel.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/ui/InCallViewModel.kt)
- Remove `Application` dependency.
- Remove `getStateLabel` and `getDurationLabel` methods.
- Update `uiState` derivation to set `CallStatus` based on `OngoingCall` state.
- Refactor `durationLabel` flow to emit raw milliseconds (Long) instead of formatted strings.

#### [MODIFY] [InCallScreen.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/ui/InCallScreen.kt)
- Implement a helper method/composable to map `CallStatus` to localized strings using `stringResource()`.
- Format the duration (received as Long) using `CommonUtils.getDurationTimeString()`.

## Verification Plan

### Automated Tests
- Build project to ensure no compilation errors.
- Unit tests for `InCallViewModel` (can now be tested without Robolectric/Android dependencies).

### Manual Verification
- Verify that call state labels (Ringing, Dialing, etc.) are still correctly displayed and localized.
- Verify that the call timer still updates correctly.
