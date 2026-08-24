# Implementation Plan: Side-Effect Extraction (Hardware)

This plan covers extracting hardware management (Proximity Sensor) from `CallsHandler` into a dedicated `CallHardwareManager`.

## Proposed Changes

### [:feature:inCall](file:///Users/alen/StudioProjects/opendialer/feature/inCall)

#### [NEW] [CallHardwareManager.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/CallHardwareManager.kt)
- Injectable `@Singleton` class.
- Injects `CallManager` and `ProximitySensor`.
- Reactively observes `CallManager.displayState` and `CallManager.audioState` using coroutines.
- Manages the proximity sensor lifecycle (updates mode based on call and audio state).

#### [MODIFY] [CallsHandler.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/CallsHandler.kt)
- Remove `proximitySensor` field and related logic.
- Remove `updateProximitySensor` method.
- Update `setup()` and `tearDown()` signatures and implementation.

#### [MODIFY] [InCallServiceImpl.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/InCallServiceImpl.kt)
- Inject `CallHardwareManager`.
- Call `hardwareManager.attach()` in `onBind`.
- Call `hardwareManager.detach()` in `onUnbind`.
- Simplify `callHandler.setup()` call.

## Verification Plan

### Automated Tests
- Build project to ensure no compilation errors.
- Run existing unit tests.

### Manual Verification
- Verify proximity sensor behavior (screen turning off when near earpiece during an active call, and staying on during speaker/bluetooth/disconnected states).
