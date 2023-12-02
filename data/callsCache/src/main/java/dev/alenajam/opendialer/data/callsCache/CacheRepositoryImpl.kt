package dev.alenajam.opendialer.data.callsCache

import android.app.Application
import android.content.Context
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import dev.alenajam.opendialer.core.aosp.UriUtils
import dev.alenajam.opendialer.core.common.exception.Failure
import dev.alenajam.opendialer.core.common.functional.Either
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CacheRepositoryImpl
@Inject constructor(private val app: Application) : CacheRepository {
  companion object {
    private val TAG = CacheRepositoryImpl::class.simpleName
  }

  private var channel: Channel<ContactInfoRequest>? = null
  private val updatedNumbers = mutableSetOf<NumberWithCountryIso>()

  override suspend fun start(): Either<Failure, Unit> {
    channel = Channel()
    channel?.let {
      for (request in it) {
        attemptUpdateContactInfo(request)
      }
    }
    return Either.Right(Unit)
  }

  override fun stop() {
    channel?.cancel()
    channel = null
  }

  private fun attemptUpdateContactInfo(request: ContactInfoRequest) {
    if (request.number === null) {
      return
    }

    /** Fetch new contact info */
    val info = getContactInfoByNumber(
      app,
      request.number,
      request.countryIso
    )

    if (info === ContactInfo.EMPTY) {
      return
    }

    /** Update call log */
    ContactInfoHelper(app).updateCallLogContactInfo(
      request.number,
      request.countryIso,
      info,
      request.callLogInfo
    )
  }

  override fun requestUpdateContactInfo(
    coroutineScope: CoroutineScope,
    number: String?,
    countryIso: String?,
    callLogInfo: ContactInfo
  ): Either<Failure, Unit> {
    val numberWithCountryIso = NumberWithCountryIso(number, countryIso)

    if (!updatedNumbers.contains(numberWithCountryIso)) {
      updatedNumbers.add(numberWithCountryIso)
      val request = ContactInfoRequest(
        numberWithCountryIso.number,
        numberWithCountryIso.countryIso,
        callLogInfo
      )

      coroutineScope.launch { channel?.send(request) }
    }

    return Either.Right(Unit)
  }

  override fun invalidate() {
    updatedNumbers.clear()
  }
}

fun getContactInfoByNumber(
  context: Context,
  number: String,
  countryIso: String?
): ContactInfo {
  val uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI

  val projection = arrayOf(
    ContactsContract.PhoneLookup.CONTACT_ID,
    ContactsContract.PhoneLookup.DISPLAY_NAME,
    ContactsContract.PhoneLookup.TYPE,
    ContactsContract.PhoneLookup.LABEL,
    ContactsContract.PhoneLookup.NUMBER,
    ContactsContract.PhoneLookup.NORMALIZED_NUMBER,
    ContactsContract.PhoneLookup.PHOTO_ID,
    ContactsContract.PhoneLookup.LOOKUP_KEY,
    ContactsContract.PhoneLookup.PHOTO_URI
  )

  context.contentResolver.query(
    uri
      .buildUpon()
      .appendPath(number)
      .build(),
    projection,
    null,
    null,
    null
  )?.let {
    if (it.moveToFirst()) {
      val lookupKey =
        it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.LOOKUP_KEY))
      val contactId =
        it.getLong(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.CONTACT_ID))
      return ContactInfo(
        name = it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)),
        number = it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.NUMBER)),
        photoUri = it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_URI))
          ?.takeIf { uri -> uri.isNotBlank() },
        type = it.getInt(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.TYPE)),
        label = it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.LABEL)),
        lookupUri = UriUtils.uriToString(
          ContactsContract.Contacts.getLookupUri(
            contactId,
            lookupKey
          )
        ),
        normalizedNumber = it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.NORMALIZED_NUMBER)),
        photoId = it.getLong(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_ID))
      )
    }
    it.close()
  }

  return createEmptyContactInfoForNumber(context, number, countryIso)
}

private fun createEmptyContactInfoForNumber(
  context: Context,
  number: String,
  countryIso: String?
): ContactInfo {
  val formattedNumber: String =
    ContactInfoHelper(context).formatPhoneNumber(number, null, countryIso)
  val normalizedNumber = PhoneNumberUtils.formatNumberToE164(number, countryIso)
  return ContactInfo(
    number = number,
    lookupUri = UriUtils.uriToString(
      ContactInfoHelper.createTemporaryContactUri(
        formattedNumber
      )
    ),
    normalizedNumber = normalizedNumber,
    formattedNumber = formattedNumber
  )
}