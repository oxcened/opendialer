package dev.alenajam.opendialer.data.calls

import android.telephony.PhoneNumberUtils
import androidx.annotation.Keep
import java.io.Serializable
import java.util.Date

@Keep
class DialerCall(
  val id: Int,
  val number: String?,
  val date: Date,
  val type: CallType,
  val options: List<CallOption>,
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
        val options = mutableListOf<CallOption>()
        // Not anonymous
        if (!call.number.isNullOrBlank()) {
          // Not contact
          if (call.name.isNullOrBlank()) {
            options.addAll(
              listOf(
                CallOption(
                  CallOption.ID_CREATE_CONTACT,
                  0
                ),
                CallOption(
                  CallOption.ID_ADD_EXISTING,
                  0
                )
              )
            )
          }

          options.add(
            CallOption(
              CallOption.ID_SEND_MESSAGE,
              0
            )
          )
        }

        // Always
        options.add(
          CallOption(
            CallOption.ID_CALL_DETAILS,
            0
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

fun equalNumbers(number1: String?, number2: String?): Boolean {
  return PhoneNumberUtils.compare(number1, number2)
}