package dev.alenajam.opendialer.features.dialer.calls.detailCall

import dev.alenajam.opendialer.features.dialer.calls.CallType
import dev.alenajam.opendialer.features.dialer.calls.DialerCallEntity
import java.io.Serializable
import java.util.Date

class DetailCall(
  val id: Int,
  val type: CallType,
  val date: Date,
  val duration: Long
) : Serializable {
  companion object {
    fun map(call: DialerCallEntity): DetailCall? {
      val type = CallType.values().find { t -> t.value == call.type }

      type?.let {
        return DetailCall(
          id = call.id,
          type = it,
          date = Date(call.date),
          duration = call.duration
        )
      }
      return null
    }
  }
}