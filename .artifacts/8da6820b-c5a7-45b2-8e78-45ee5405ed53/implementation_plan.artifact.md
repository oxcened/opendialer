# Java to Kotlin Migration: InCall Service Components

This plan covers the migration of several core service components in the `:feature:inCall` module from Java to Kotlin.

## User Review Required

> [!NOTE]
> `OngoingCallHelper` and `TelecomAdapter` are already Kotlin files in the project. I will focus on the remaining Java files and ensuring the entire `service` package is consistent.

I also recommend migrating the following files that are tightly coupled with the requested ones to ensure full null-safety and idiomatic Kotlin usage across the `service` package:
- `ProximitySensor.java`
- `NotificationHelper.java`
- `CallContactResolver.java`
- `CallDisplaySelector.java`

## Proposed Changes

### [:feature:inCall](file:///Users/alen/StudioProjects/opendialer/feature/inCall)

#### [MODIFY] [OngoingCallHelper.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/OngoingCallHelper.kt)
- Minor cleanup if necessary to match the new Kotlin style.

#### [NEW] [InCallServiceImpl.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/InCallServiceImpl.kt)
#### [DELETE] [InCallServiceImpl.java](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/InCallServiceImpl.java)
- Convert to Kotlin, using property injection for Hilt.

#### [NEW] [OngoingCall.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/OngoingCall.kt)
#### [DELETE] [OngoingCall.java](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/OngoingCall.java)
- Convert to Kotlin.
- Use Kotlin `lazy` or properties for `startTime` and `totalTime`.
- Refactor `Call.Callback` to a cleaner Kotlin syntax.

#### [NEW] [CallsHandler.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/CallsHandler.kt)
#### [DELETE] [CallsHandler.java](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/CallsHandler.java)
- Convert to Kotlin.
- Use `StateFlow` or keep `LiveData` depending on existing architecture (likely keep `LiveData` for minimal impact, but can be updated to `StateFlow` if preferred).
- Improve collection handling with Kotlin stdlib.

#### [NEW] [ProximitySensor.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/ProximitySensor.kt)
#### [DELETE] [ProximitySensor.java](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/ProximitySensor.java)
- (Recommended) Convert to Kotlin.

#### [NEW] [NotificationHelper.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/NotificationHelper.kt)
#### [DELETE] [NotificationHelper.java](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/NotificationHelper.java)
- (Recommended) Convert to Kotlin.

#### [NEW] [CallContactResolver.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/CallContactResolver.kt)
#### [DELETE] [CallContactResolver.java](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/CallContactResolver.java)
- (Recommended) Convert to Kotlin.

#### [NEW] [CallDisplaySelector.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/CallDisplaySelector.kt)
#### [DELETE] [CallDisplaySelector.java](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/CallDisplaySelector.java)
- (Recommended) Convert to Kotlin.

## Verification Plan

### Automated Tests
- Run `CallDisplaySelectorTest` (after migrating it to Kotlin as well).
- Run `:feature:inCall:unitTest` to ensure no regressions.

### Manual Verification
- Deploy the app and test call handling:
  - Incoming calls (notification and UI).
  - Outgoing calls.
  - Conference calls (adding, merging, splitting).
  - Proximity sensor behavior.
  - Audio route changes (speaker, bluetooth).
