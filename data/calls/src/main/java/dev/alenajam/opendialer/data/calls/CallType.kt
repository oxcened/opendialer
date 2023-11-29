package dev.alenajam.opendialer.data.calls

enum class CallType(val value: Int) {
  INCOMING(1),

  /** Call log type for outgoing calls. */
  OUTGOING(2),

  /** Call log type for missed calls. */
  MISSED(3),

  /** Call log type for voicemails. */
  VOICEMAIL(4),

  /** Call log type for calls rejected by direct user action. */
  REJECTED(5),

  /** Call log type for calls blocked automatically. */
  BLOCKED(6),

  /**
   * Call log type for a call which was answered on another device.  Used in situations where
   * a call rings on multiple devices simultaneously and it ended up being answered on a
   * device other than the current one.
   */
  ANSWERED_EXTERNALLY(7)
}