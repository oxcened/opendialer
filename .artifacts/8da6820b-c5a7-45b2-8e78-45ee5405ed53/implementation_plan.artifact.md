# Implementation Plan: Expandable Search Results in Dialpad

This plan covers enhancing the search results in the dialpad with expandable rows and quick actions, consistent with the rest of the app.

## Proposed Changes

### [:feature:contactsSearch](file:///Users/alen/StudioProjects/opendialer/feature/contactsSearch)

#### [MODIFY] [ContactsSearchScreen.kt](file:///Users/alen/StudioProjects/opendialer/feature/contactsSearch/src/main/java/dev/alenajam/opendialer/feature/contactsSearch/ContactsSearchScreen.kt)
- Add `openRowKey` state to track the expanded row.
- Update `SearchList` to pass `isOpen` and `onClick` to `ResultRow`.
- Update `ResultRow` to:
    - Support expansion using `AnimatedVisibility`.
    - Include a `Phone` icon button on the far right for immediate calling.
    - Show actions like "Message", "Add to contact" (if not saved), "Open contact" (if saved), and "History" in the expanded area.
- Add `ResultActionRow` helper composable (similar to `CallRowButton` or `ContactActionRow`).

#### [MODIFY] [SearchContactsViewModel.kt](file:///Users/alen/StudioProjects/opendialer/feature/contactsSearch/src/main/java/dev/alenajam/opendialer/feature/contactsSearch/SearchContactsViewModel.kt)
- Add `openContact(activity: Activity, contactId: Int)` method.
- (Optional) Add `getHistoryIds` if I decide to implement the History action fully, otherwise I'll use a placeholder or simpler approach for now as it might require injecting more dependencies.

## Verification Plan

### Automated Tests
- Build project to ensure no compilation errors.

### Manual Verification
- Perform a search in the dialpad.
- Verify that tapping a result row expands it.
- Verify that the call button on the right works.
- Verify that expanded actions (Message, etc.) work correctly.
