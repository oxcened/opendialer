# Implementation Plan: Call Log Enhancements & Favorite Management

This plan covers improving the call log filtering, aligning UI headers, and enhancing favorite contact management with a long-press menu and simplified picker.

## User Review Required

> [!IMPORTANT]
> - Long-pressing a favorite contact will now show a "Remove from favorites" option.
> - The "Add to favorites" screen will now close immediately after picking a contact.
> - Call log filters (Missed, Contacts, Non-spam) will now correctly filter the list.

## Proposed Changes

### [:feature:calls](file:///Users/alen/StudioProjects/opendialer/feature/calls)

#### [MODIFY] [CallsScreen.kt](file:///Users/alen/StudioProjects/opendialer/feature/calls/src/main/java/dev/alenajam/opendialer/feature/calls/CallsScreen.kt)
- **Point 1 (Remove Favorite):** Add a `DropdownMenu` to `FavoriteItem` triggered by long-press. Use `Modifier.combinedClickable` (requires `ExperimentalFoundationApi`).
- **Point 3 (Header Alignment):** Update `CallDateHeader` to match the "Favorites" header style (typography, padding, and capitalization).
- **Point 4 (Filtering):** Implement filtering logic for "Contacts" (where `isContactSaved()` is true) and "Non-spam" (not blocked) in the `filteredCalls` remember block.

#### [MODIFY] [CallsViewModel.kt](file:///Users/alen/StudioProjects/opendialer/feature/calls/src/main/java/dev/alenajam/opendialer/feature/calls/CallsViewModel.kt)
- Add `unstarContact(contactId: Int)` to handle removal from favorites.

### [:feature:contacts](file:///Users/alen/StudioProjects/opendialer/feature/contacts)

#### [MODIFY] [AddFavoriteScreen.kt](file:///Users/alen/StudioProjects/opendialer/feature/contacts/src/main/java/dev/alenajam/opendialer/feature/contacts/AddFavoriteScreen.kt)
- **Point 2 (Simplified Picker):**
    - Remove the Star icon from `FavoritePickerRow`.
    - Update `FavoritePickerRow` to call `onToggleFavorite` and immediately trigger a callback to close the screen.

## Verification Plan

### Automated Tests
- Build project and ensure no compilation errors.

### Manual Verification
- **Remove Favorite:** Long-press a favorite, select remove, and verify it disappears from the bar.
- **Add Favorite:** Go to Add, tap a contact, verify the screen closes and the contact is added.
- **Header Alignment:** Visually check that "Favorites" and "Today" headers look identical.
- **Filtering:** Tap "Missed", "Contacts", and "Non-spam" chips and verify the list filters correctly.
