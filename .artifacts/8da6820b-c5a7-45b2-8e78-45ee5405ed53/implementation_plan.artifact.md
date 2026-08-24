# Implementation Plan: Notification Modernization & Reactive Call State

This plan covers modernizing the call notification system using `Notification.CallStyle` and refactoring `OngoingCall` to expose state reactively via `StateFlow`.

## Proposed Changes

### [:feature:inCall](file:///Users/alen/StudioProjects/opendialer/feature/inCall)

#### [MODIFY] [NotificationHelper.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/NotificationHelper.kt)
- Clean up dead code and commented-out sections.
- Refactor `notifyCall` to use `Notification.CallStyle` for Android 12+ (API 31+).
- Use `Person` API for the notification sender.
- Ensure proper mapping of Telecom actions (Answer/Hangup) to notification actions.
- Convert `object` to a proper injectable class if it helps with state management, but keeping as `object` is also fine for now if it stays stateless.

#### [MODIFY] [OngoingCall.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/OngoingCall.kt)
- Define an `OngoingCallInfo` data class to hold immutable snapshot of call state.
- Expose a `StateFlow<OngoingCallInfo>` from `OngoingCall`.
- Remove the `Listener` interface.
- Ensure all property updates trigger a new emission in the `StateFlow`.

#### [MODIFY] [CallsHandler.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/CallsHandler.kt)
- Remove `OngoingCall.Listener` implementation.
- Observe each `OngoingCall`'s `StateFlow` to trigger `updateCalls()`.
- *Optimization:* Instead of full collection of all flows, just having the `OngoingCall` notify the handler via a simple function call is still technically a "listener", but we can make it more reactive by having `CallsHandler` build its state by combining the flows of active calls.

## Verification Plan

### Automated Tests
- Build the project and ensure all components compile.
- Run unit tests for `CallDisplaySelector`.

### Manual Verification
- Verify the new notification style on Android 12+ devices.
- Test incoming call actions directly from the notification.
- Verify UI updates in the app when call details change (e.g., contact resolved).
- Test call duration and state transitions.
