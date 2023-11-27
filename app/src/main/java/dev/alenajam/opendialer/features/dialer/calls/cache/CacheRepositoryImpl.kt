package dev.alenajam.opendialer.features.dialer.calls.cache

import android.app.Application
import dev.alenajam.opendialer.core.exception.Failure
import dev.alenajam.opendialer.core.functional.Either
import dev.alenajam.opendialer.features.dialer.contacts.ContactsData
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
    val info = ContactsData.getContactInfoByNumber(
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