package dev.alenajam.opendialer.features.dialer

import android.content.ContentResolver
import android.content.ContentValues
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.provider.BlockedNumberContract
import android.provider.CallLog
import dev.alenajam.opendialer.R
import dev.alenajam.opendialer.core.exception.Failure
import dev.alenajam.opendialer.core.functional.Either
import dev.alenajam.opendialer.features.dialer.calls.CallsData
import dev.alenajam.opendialer.features.dialer.calls.DialerCall
import dev.alenajam.opendialer.features.dialer.calls.DialerCallEntity
import dev.alenajam.opendialer.features.dialer.calls.detailCall.DetailCall
import dev.alenajam.opendialer.features.dialer.contacts.ContactsData
import dev.alenajam.opendialer.features.dialer.contacts.DialerContactEntity
import dev.alenajam.opendialer.features.dialer.searchContacts.DialerSearchContactEntity
import dev.alenajam.opendialer.features.dialer.searchContacts.SearchContactsData
import dev.alenajam.opendialer.features.dialer.searchContacts.SearchContactsDialpadData
import dev.alenajam.opendialer.model.CallOption
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class DialerRepositoryImpl
@Inject constructor(private val app: dev.alenajam.opendialer.App) : DialerRepository {
  @ExperimentalCoroutinesApi
  override fun getCalls(contentResolver: ContentResolver): Flow<List<DialerCallEntity>> =
    callbackFlow {
      val observer = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
          CallsData.getCursor(contentResolver)?.let {
            trySend(CallsData.getData(it))
            it.close()
          }
        }
      }

      contentResolver.registerContentObserver(CallsData.URI, true, observer)

      CallsData.getCursor(contentResolver)?.let {
        trySend(CallsData.getData(it))
        it.close()
      }

      awaitClose {
        contentResolver.unregisterContentObserver(observer)
      }
    }

  @ExperimentalCoroutinesApi
  override fun getContacts(contentResolver: ContentResolver): Flow<List<DialerContactEntity>> =
    callbackFlow {
      val observer = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
          ContactsData.getCursor(contentResolver)?.let {
            trySend(ContactsData.getData(it))
            it.close()
          }
        }
      }

      contentResolver.registerContentObserver(ContactsData.URI, true, observer)

      ContactsData.getCursor(contentResolver)?.let {
        trySend(ContactsData.getData(it))
        it.close()
      }

      awaitClose {
        contentResolver.unregisterContentObserver(observer)
      }
    }

  override suspend fun searchContacts(
    contentResolver: ContentResolver,
    query: String
  ): Either<Failure, List<DialerSearchContactEntity>> {
    SearchContactsData.getCursor(contentResolver, query)?.let {
      val data = SearchContactsData.getData(it)
      it.close()
      return Either.Right(data)
    }

    return Either.Left(Failure.NoData)
  }

  override suspend fun searchContactsDialpad(
    contentResolver: ContentResolver,
    query: String
  ): Either<Failure, List<DialerSearchContactEntity>> {
    SearchContactsDialpadData.getCursor(contentResolver)?.let {
      val data = SearchContactsDialpadData.getData(app, it, query)
      it.close()
      return Either.Right(data)
    }

    return Either.Left(Failure.NoData)
  }

  override suspend fun getDetailOptions(call: DialerCall): Either<Failure, List<CallOption>> {
    return with(app) {
      val options = mutableListOf<CallOption>()

      if (!call.isAnonymous()) {
        options.addAll(
          listOf(
            CallOption(CallOption.ID_COPY_NUMBER, R.drawable.icon_06),
            CallOption(
              CallOption.ID_EDIT_BEFORE_CALL,
              R.drawable.icon_07
            )
          )
        )
      }

      options.add(
        CallOption(
          CallOption.ID_DELETE,
          R.drawable.icon_11
        )
      )

      if (!call.isAnonymous()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          val hasDefault = dev.alenajam.opendialer.util.DefaultPhoneUtils.hasDefault(this)
          val canUserBlockNumbers = BlockedNumberContract.canCurrentUserBlockNumbers(this)
          if (hasDefault && canUserBlockNumbers) {
            val isBlocked =
              BlockedNumberContract.isBlocked(this, call.contactInfo.number)
            val blockOption = CallOption(
              if (isBlocked) CallOption.ID_UNBLOCK_CALLER else CallOption.ID_BLOCK_CALLER,
              R.drawable.icon_10
            )
            options.add(blockOption)
          }
        }
      }

      Either.Right(options)
    }
  }

  override suspend fun deleteCalls(calls: List<DetailCall>): Either<Failure, Unit> {
    if (!dev.alenajam.opendialer.util.PermissionUtils.hasRecentsPermission(app)) {
      return Either.Left(Failure.NotPermitted)
    }

    var deleted = 0
    calls.forEach {
      deleted += app.contentResolver.delete(
        CallLog.Calls.CONTENT_URI,
        "${CallLog.Calls._ID} = ${it.id}",
        null
      )
    }

    return if (deleted > 0) Either.Right(Unit) else Either.Left(Failure.LocalFailure)
  }

  override suspend fun blockCaller(number: String): Either<Failure, Unit> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
      return Either.Left(Failure.NotPermitted)
    }

    val values = ContentValues().apply {
      put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number)
    }
    val uri: Uri? =
      app.contentResolver.insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI, values)
    return if (uri == null) Either.Left(Failure.LocalFailure) else Either.Right(Unit)
  }

  override suspend fun unblockCaller(number: String): Either<Failure, Unit> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
      return Either.Left(Failure.NotPermitted)
    }

    val blocked = BlockedNumberContract.unblock(app, number)
    return if (blocked < 1) Either.Left(Failure.LocalFailure) else Either.Right(Unit)
  }
}