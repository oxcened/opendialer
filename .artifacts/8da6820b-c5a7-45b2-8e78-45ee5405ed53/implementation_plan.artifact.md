# Implementation Plan: Core Common Modernization

This plan covers the migration of core utility and model classes in the `:core:common` module from Java to Kotlin.

## Proposed Changes

### [:core:common](file:///Users/alen/StudioProjects/opendialer/core/common)

#### [NEW] [Contact.kt](file:///Users/alen/StudioProjects/opendialer/core/common/src/main/java/dev/alenajam/opendialer/core/common/Contact.kt)
#### [DELETE] [Contact.java](file:///Users/alen/StudioProjects/opendialer/core/common/src/main/java/dev/alenajam/opendialer/core/common/Contact.java)
- Convert to a Kotlin `data class`.
- Use default parameters to replace multiple constructors.
- Maintain `Serializable` for compatibility.

#### [NEW] [ContactsHelper.kt](file:///Users/alen/StudioProjects/opendialer/core/common/src/main/java/dev/alenajam/opendialer/core/common/ContactsHelper.kt)
#### [DELETE] [ContactsHelper.java](file:///Users/alen/StudioProjects/opendialer/core/common/src/main/java/dev/alenajam/opendialer/core/common/ContactsHelper.java)
- Convert to a Kotlin `object`.
- Keep the `getContactByPhoneNumber` logic.

#### [NEW] [PermissionUtils.kt](file:///Users/alen/StudioProjects/opendialer/core/common/src/main/java/dev/alenajam/opendialer/core/common/PermissionUtils.kt)
#### [DELETE] [PermissionUtils.java](file:///Users/alen/StudioProjects/opendialer/core/common/src/main/java/dev/alenajam/opendialer/core/common/PermissionUtils.java)
- Convert to a Kotlin `object`.

#### [NEW] [DefaultPhoneUtils.kt](file:///Users/alen/StudioProjects/opendialer/core/common/src/main/java/dev/alenajam/opendialer/core/common/DefaultPhoneUtils.kt)
#### [DELETE] [DefaultPhoneUtils.java](file:///Users/alen/StudioProjects/opendialer/core/common/src/main/java/dev/alenajam/opendialer/core/common/DefaultPhoneUtils.java)
- Convert to a Kotlin `object`.

#### [NEW] [SharedPreferenceHelper.kt](file:///Users/alen/StudioProjects/opendialer/core/common/src/main/java/dev/alenajam/opendialer/core/common/SharedPreferenceHelper.kt)
#### [DELETE] [SharedPreferenceHelper.java](file:///Users/alen/StudioProjects/opendialer/core/common/src/main/java/dev/alenajam/opendialer/core/common/SharedPreferenceHelper.java)
- Convert to a Kotlin `class` (it's not abstract/static).

#### [NEW] [MyDialog.kt](file:///Users/alen/StudioProjects/opendialer/core/common/src/main/java/dev/alenajam/opendialer/core/common/MyDialog.kt)
#### [DELETE] [MyDialog.java](file:///Users/alen/StudioProjects/opendialer/core/common/src/main/java/dev/alenajam/opendialer/core/common/MyDialog.java)
- Convert to Kotlin.
- Use property access for views.

#### [NEW] [CommonUtils.kt](file:///Users/alen/StudioProjects/opendialer/core/common/src/main/java/dev/alenajam/opendialer/core/common/CommonUtils.kt)
#### [DELETE] [CommonUtils.java](file:///Users/alen/StudioProjects/opendialer/core/common/src/main/java/dev/alenajam/opendialer/core/common/CommonUtils.java)
- Convert to a Kotlin `object`.
- This is a large file, so I'll handle it carefully.

## Verification Plan

### Automated Tests
- Run all unit tests in the project (since `:core:common` is used everywhere).
- Build the project to ensure no Java interop issues.

### Manual Verification
- Verify contact lookup still works.
- Verify permission checks are still correct.
- Verify call making/SMS logic (in `CommonUtils`) still works.
