# Call reliability checklist

Run this checklist before a release that changes calling, notifications, permissions, or the
default-dialer flow. Use a physical device where possible; Bluetooth and lock-screen behavior are
not representative on every emulator image.

## Default dialer and permissions

- Set OpenDialer as the default phone app, restart it, and verify that the recents, contacts, and
  dialpad screens are available.
- Decline each runtime permission once. Open the corresponding feature again and verify that the
  app explains the missing capability without crashing.
- Revoke Contacts, Call Log, Phone, and Notifications permissions individually in Android Settings
  while the app is backgrounded. Return to the app and verify that it refreshes its state.
- Remove OpenDialer as the default dialer, return to the app, then set it as default again.

## Incoming and outgoing calls

- Receive a call while the device is unlocked, locked, and while OpenDialer is backgrounded.
- Answer, decline, and allow a call to time out. Verify that an unanswered call creates exactly one
  missed-call notification with working Call back, Message, and app-open actions.
- Start an outgoing call from the dialpad, a contact, a favorite, and call history.
- While a call is active, background OpenDialer, rotate the device, and recreate the activity from
  the system recents screen. Verify that the in-call controls retain the real telecom state.

## Audio and multi-call behavior

- Switch between earpiece, speaker, wired headset, and Bluetooth when the device supports them.
- Use headset hardware controls to answer and hang up.
- Hold and resume a call. Start or receive a second call, then verify swap, merge, and split where
  the carrier supports those capabilities.
- Verify that ending one call leaves another active or held call usable and visible.

## Notifications and cleanup

- Verify that an ongoing-call notification is removed once the final call ends.
- Tap each missed-call notification action after the app has been force-stopped and reopened.
- Verify that an incoming call rejected from OpenDialer does not create a missed-call notification.
