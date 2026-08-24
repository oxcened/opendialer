# Implementation Plan: InCall Service Refactoring & Optimization

This plan addresses the architectural and performance issues identified in the `:feature:inCall` module, focusing on state management, flow unification, and UI optimization.

## User Review Required

> [!IMPORTANT]
> I am introducing a `CallManager` interface to unify audio commands and call actions. This will change how the ViewModel interacts with the service layer.
> I will also migrate `CallsHandler` to use `StateFlow`, which is more idiomatic for Kotlin-based Composable state management.

## Proposed Changes

### [:feature:inCall](file:///Users/alen/StudioProjects/opendialer/feature/inCall)

#### [NEW] [CallManager.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/CallManager.kt)
- Define a unified interface for all call-related actions (hangup, answer, hold, mute, speaker, etc.).
- Inherits from `InCallCommands`.

#### [MODIFY] [CallsHandler.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/CallsHandler.kt)
- Implement `CallManager`.
- Migrate `LiveData` to `StateFlow` (`calls`, `displayState`, `audioState`, `canAddCall`).
- Decouple from `InCallActivity` (remove activity reference and methods).
- Add specific action methods (hangup, answer, etc.) that delegate to `OngoingCall` or `TelecomAdapter`.

#### [MODIFY] [InCallCommandsModule.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/InCallCommandsModule.kt)
- Update to provide `CallManager` instead of just `InCallCommands`.

#### [MODIFY] [OngoingCall.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/OngoingCall.kt)
- Decouple from `CallsHandler` by using a callback or shared state for UI updates.
- Ensure all time-related logic uses `SystemClock.elapsedRealtime()` explicitly for clarity.

#### [MODIFY] [InCallViewModel.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/ui/InCallViewModel.kt)
- Use `CallManager` for all interactions.
- Refactor `refreshUiState` to be less expensive.
- Expose a dedicated `durationLabel` Flow that updates independently of the main UI state to avoid full recompositions every second.
- Collect `StateFlow` from `CallsHandler`.

#### [MODIFY] [InCallActivity.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/ui/InCallActivity.kt)
- Remove manual lifecycle registration with `CallsHandler`.

## Verification Plan

### Automated Tests
- Update and run `CallDisplaySelectorTest`.
- Add unit tests for `CallsHandler`'s implementation of `CallManager`.

### Manual Verification
- Test all call actions (Answer, Hangup, Hold, Merge, Split).
- Verify audio route switching.
- Verify duration timer accuracy and performance.
- Verify proximity sensor behavior.
