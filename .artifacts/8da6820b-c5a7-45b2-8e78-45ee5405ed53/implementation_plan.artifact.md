# Implementation Plan: Service Layer Modernization (Coroutines & DI)

This plan covers refactoring `CallContactResolver` to use Coroutines and making `ProximitySensor` fully injectable via Hilt.

## Proposed Changes

### [:feature:inCall](file:///Users/alen/StudioProjects/opendialer/feature/inCall)

#### [MODIFY] [CallContactResolver.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/CallContactResolver.kt)
- Remove `ExecutorService` and `Handler`.
- Inject a `CoroutineScope` (annotated with `@ApplicationScope` if available, or use `Dispatchers.IO` directly).
- Refactor `resolve` to use `withContext(Dispatchers.IO)`.
- Keep the `Callback` for now to avoid breaking `CallsHandler`, but internal implementation will be coroutine-based.

#### [MODIFY] [ProximitySensor.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/ProximitySensor.kt)
- Add `@Inject constructor(@ApplicationContext context: Context)`.
- Mark as `@Singleton` if appropriate (shared across call lifecycle).

#### [MODIFY] [InCallServiceImpl.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/InCallServiceImpl.kt)
- Inject `ProximitySensor` via Hilt.
- Pass the injected sensor to `callHandler.setup()`.

#### [MODIFY] [CallsHandler.kt](file:///Users/alen/StudioProjects/opendialer/feature/inCall/src/main/java/dev/alenajam/opendialer/feature/inCall/service/CallsHandler.kt)
- Ensure the `resolveContact` call still works with the refactored resolver.

## Verification Plan

### Automated Tests
- Build the project to ensure DI graphs are correct.
- Run existing unit tests.

### Manual Verification
- Verify contact resolution in the In-Call UI.
- Verify proximity sensor behavior (screen turning off when near earpiece).
