package dev.alenajam.opendialer.features.dialer.calls

import androidx.annotation.Keep
import dev.alenajam.opendialer.R
import dev.alenajam.opendialer.features.dialer.calls.cache.ContactInfo
import dev.alenajam.opendialer.features.dialer.calls.detailCall.DetailCall
import dev.alenajam.opendialer.model.CallOption
import dev.alenajam.opendialer.util.equalNumbers
import java.io.Serializable
import java.util.Date

@Keep
class DialerCall(
  val id: Int,
  val number: String?,
  val date: Date,
  val type: CallType,
  val options: List<dev.alenajam.opendialer.model.CallOption>,
  var childCalls: List<DetailCall>,
  val countryIso: String? = null,
  val contactInfo: ContactInfo
) : Serializable {
  companion object {
    fun mapList(list: List<DialerCallEntity>): List<DialerCall> {
      val calls = mutableListOf<DialerCall>()
      list.forEach {
        if (
        //      Never group anonymous calls
          !it.number.isNullOrBlank()
          && calls.isNotEmpty()
        ) {
          val last = calls.last()
          if (equalNumbers(last.contactInfo.number, it.number)) {
            DetailCall.map(it)?.let { call ->
              last.childCalls = last.childCalls.plus(call)
            }
            return@forEach
          }
        }

        map(it)?.let { call -> calls.add(call) }
      }
      return calls
    }

    fun map(call: DialerCallEntity): DialerCall? {
      val type = CallType.values().find { t -> t.value == call.type }

      type?.let { t ->
        val options = mutableListOf<dev.alenajam.opendialer.model.CallOption>()
        // Not anonymous
        if (!call.number.isNullOrBlank()) {
          // Not contact
          if (call.name.isNullOrBlank()) {
            options.addAll(
              listOf(
                dev.alenajam.opendialer.model.CallOption(
                  CallOption.ID_CREATE_CONTACT,
                  R.drawable.icon_04
                ),
                dev.alenajam.opendialer.model.CallOption(
                  CallOption.ID_ADD_EXISTING,
                  R.drawable.icon_04
                )
              )
            )
          }

          options.add(
            dev.alenajam.opendialer.model.CallOption(
              CallOption.ID_SEND_MESSAGE,
              R.drawable.icon_05
            )
          )
        }

        // Always
        options.add(
          dev.alenajam.opendialer.model.CallOption(
            CallOption.ID_CALL_DETAILS,
            R.drawable.icon_08
          )
        )

        val contactNumber = call.matchedNumber ?: (call.number + call.postDialDigits)

        return DialerCall(
          id = call.id,
          number = call.number,
          date = Date(call.date),
          type = t,
          options = options,
          childCalls = listOfNotNull(DetailCall.map(call)),
          countryIso = call.countryIso,
          contactInfo = ContactInfo(
            name = call.name,
            number = contactNumber,
            photoUri = call.photoUri,
            type = call.numberType,
            label = call.label,
            lookupUri = call.lookupUri,
            normalizedNumber = call.normalizedNumber,
            formattedNumber = call.formattedNumber,
            geoDescription = call.geoDescription,
            photoId = call.photoId
          )
        )
      }
      return null
    }
  }

  fun isAnonymous(): Boolean = contactInfo.number.isNullOrBlank()
}