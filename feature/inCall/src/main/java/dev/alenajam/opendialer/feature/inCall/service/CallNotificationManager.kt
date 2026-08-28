package dev.alenajam.opendialer.feature.inCall.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.telecom.Call
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.alenajam.opendialer.core.common.ui.contactAvatarColors
import dev.alenajam.opendialer.core.common.ui.contactAvatarColorKey
import dev.alenajam.opendialer.feature.inCall.R
import dev.alenajam.opendialer.feature.inCall.ui.InCallActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallNotificationManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val callManager: CallManager
) {
    companion object {
        private const val CHANNEL_ID_INCOMING_CALLS = "dev.alenajam.opendialer.notification_channel.incoming_calls"
        private const val CHANNEL_ID_ONGOING_CALLS = "dev.alenajam.opendialer.notification_channel.ongoing_calls"
        private const val CHANNEL_ID_OUTGOING_CALLS = "dev.alenajam.opendialer.notification_channel.outgoing_calls"
        private const val CHANNEL_ID_MISSED_CALLS = "dev.alenajam.opendialer.notification_channel.missed_calls"
        private const val NOTIFICATION_ID_CALL = 1
        private const val INTENT_ACTION_CALL_BUTTON_CLICK_ACCEPT = "dev.alenajam.opendialer.CALL_ACCEPT"
        private const val INTENT_ACTION_CALL_BUTTON_CLICK_DECLINE = "dev.alenajam.opendialer.CALL_DECLINE"
    }

    private var callService: InCallServiceImpl? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observationJob: Job? = null
    private var eventObservationJob: Job? = null

    init {
        setupNotificationChannels()
    }

    fun attach(service: InCallServiceImpl) {
        this.callService = service
        startObserving()
    }

    fun detach() {
        observationJob?.cancel()
        observationJob = null
        eventObservationJob?.cancel()
        eventObservationJob = null
        removeCallNotification()
        this.callService = null
    }

    private fun startObserving() {
        observationJob?.cancel()
        observationJob = scope.launch {
            callManager.displayState.collectLatest { state ->
                val primary = state.primary
                if (primary != null) {
                    handleCallNotification(primary, primary.state)
                } else {
                    removeCallNotification()
                }
            }
        }
        eventObservationJob = scope.launch {
            callManager.events.collectLatest { event ->
                if (event is CallEvent.MissedCall) notifyMissedCall(event)
            }
        }
    }

    private fun handleCallNotification(call: OngoingCall, state: Int) {
        when (state) {
            Call.STATE_RINGING -> notifyIncomingCall(call)
            Call.STATE_DIALING -> notifyOutgoingCall(call)
            Call.STATE_ACTIVE -> notifyOngoingCall(call)
            Call.STATE_HOLDING -> notifyOnHoldCall(call)
            Call.STATE_DISCONNECTING -> notifyDisconnectingCall(call)
            else -> removeCallNotification()
        }
    }

    private fun setupNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java) ?: return
            val channels = listOf(
                NotificationChannel(CHANNEL_ID_INCOMING_CALLS, context.getString(R.string.channel_incoming_calls), NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_ID_ONGOING_CALLS, context.getString(R.string.channel_ongoing_calls), NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(CHANNEL_ID_OUTGOING_CALLS, context.getString(R.string.channel_outgoing_calls), NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(CHANNEL_ID_MISSED_CALLS, context.getString(R.string.channel_missed_calls), NotificationManager.IMPORTANCE_DEFAULT)
            )
            channels
                .filter { it.id != CHANNEL_ID_MISSED_CALLS }
                .forEach { it.setSound(null, null) }
            nm.createNotificationChannels(channels)
        }
    }

    private fun notifyCall(channelId: String, call: OngoingCall, type: CallType) {
        val caller = callerLabel(call.callerName, call.callerNumber)
        val intent = Intent(context, InCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT
        }
        val pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, channelId)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }

        builder.setSmallIcon(R.drawable.ic_notification_call)
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_CALL)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(true)

        // A CallStyle notification does not make the activity full-screen by itself.
        // This is needed on Android 12+ as well as on older releases so the ringing
        // call can be presented when the device is locked.
        if (type == CallType.INCOMING && canUseFullScreenIntent()) {
            builder.setFullScreenIntent(pendingIntent, true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val person = Person.Builder()
                .setName(caller)
                .setIcon(createCallerIcon(call))
                .setImportant(true)
                .build()
            val style = when (type) {
                CallType.INCOMING -> Notification.CallStyle.forIncomingCall(person, getDeclineIntent(), getAcceptIntent())
                CallType.ONGOING -> Notification.CallStyle.forOngoingCall(person, getDeclineIntent())
                CallType.OUTGOING -> Notification.CallStyle.forOngoingCall(person, getDeclineIntent())
            }
            builder.style = style
        } else {
            builder.setContentTitle(caller)
            builder.setContentText(when (type) {
                CallType.INCOMING -> context.getString(R.string.notification_incoming_call_title, caller)
                CallType.ONGOING -> context.getString(R.string.notification_ongoing_call_title, caller)
                CallType.OUTGOING -> context.getString(R.string.notification_outgoing_call_title, caller)
            })
            if (type == CallType.INCOMING) {
                builder.addAction(Notification.Action.Builder(null, context.getString(android.R.string.cancel), getDeclineIntent()).build())
                builder.addAction(Notification.Action.Builder(null, context.getString(android.R.string.ok), getAcceptIntent()).build())
            }
        }

        callService?.let { service ->
            val notification = builder.build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                service.startForeground(NOTIFICATION_ID_CALL, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)
            } else {
                service.startForeground(NOTIFICATION_ID_CALL, notification)
            }
        }
    }

    private fun getAcceptIntent(): PendingIntent {
        val intent = Intent(context, CallButtonsListener::class.java).apply { action = INTENT_ACTION_CALL_BUTTON_CLICK_ACCEPT }
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun getDeclineIntent(): PendingIntent {
        val intent = Intent(context, CallButtonsListener::class.java).apply { action = INTENT_ACTION_CALL_BUTTON_CLICK_DECLINE }
        return PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun notifyIncomingCall(call: OngoingCall) = notifyCall(CHANNEL_ID_INCOMING_CALLS, call, CallType.INCOMING)
    private fun notifyOutgoingCall(call: OngoingCall) = notifyCall(CHANNEL_ID_OUTGOING_CALLS, call, CallType.OUTGOING)
    private fun notifyOngoingCall(call: OngoingCall) = notifyCall(CHANNEL_ID_ONGOING_CALLS, call, CallType.ONGOING)
    private fun notifyOnHoldCall(call: OngoingCall) = notifyCall(CHANNEL_ID_ONGOING_CALLS, call, CallType.ONGOING)
    private fun notifyDisconnectingCall(call: OngoingCall) = notifyCall(CHANNEL_ID_ONGOING_CALLS, call, CallType.ONGOING)

    private fun canUseFullScreenIntent(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return true
        return context.getSystemService(NotificationManager::class.java)
            ?.canUseFullScreenIntent() == true
    }

    private fun notifyMissedCall(call: CallEvent.MissedCall) {
        val caller = callerLabel(call.callerName, call.callerNumber)
        val openDialerIntent = (
            context.packageManager.getLaunchIntentForPackage(context.packageName)
                ?: Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setPackage(context.packageName)
            ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openDialerPendingIntent = PendingIntent.getActivity(
            context,
            call.notificationId,
            openDialerIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID_MISSED_CALLS)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }
        builder.setSmallIcon(R.drawable.ic_notification_call)
            .setContentTitle(context.getString(R.string.notification_missed_call_title, caller))
            .setContentIntent(openDialerPendingIntent)
            .setCategory(Notification.CATEGORY_MISSED_CALL)
            .setVisibility(Notification.VISIBILITY_PRIVATE)
            .setAutoCancel(true)

        if (call.callerNumber.isNotBlank()) {
            val callBackIntent = Intent(Intent.ACTION_CALL, Uri.fromParts("tel", call.callerNumber, null))
            val callBackPendingIntent = PendingIntent.getActivity(
                context,
                call.notificationId,
                callBackIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.addAction(
                Notification.Action.Builder(
                    null,
                    context.getString(R.string.notification_call_back),
                    callBackPendingIntent
                ).build()
            )
            val messageIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", call.callerNumber, null))
            val messagePendingIntent = PendingIntent.getActivity(
                context,
                call.notificationId + 1,
                messageIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.addAction(
                Notification.Action.Builder(
                    null,
                    context.getString(R.string.notification_message),
                    messagePendingIntent
                ).build()
            )
        }

        context.getSystemService(NotificationManager::class.java)
            ?.notify(call.notificationId, builder.build())
    }

    private fun createCallerIcon(call: OngoingCall): Icon {
        call.callerImageUri?.takeIf { it.isNotBlank() }?.let { uri ->
            return Icon.createWithContentUri(Uri.parse(uri))
        }

        val name = call.callerName?.takeIf { it.isNotBlank() && it != call.callerNumber }
        val colors = contactAvatarColors(contactAvatarColorKey(name, call.callerNumber))
        val bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = colors.background.toInt()
        canvas.drawCircle(64f, 64f, 64f, paint)
        paint.color = colors.foreground.toInt()

        if (name.isNullOrBlank()) {
            canvas.drawCircle(64f, 43f, 17f, paint)
            canvas.drawRoundRect(RectF(34f, 66f, 94f, 112f), 30f, 30f, paint)
        } else {
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            paint.textSize = 72f
            val baseline = 64f - (paint.ascent() + paint.descent()) / 2f
            canvas.drawText(name.take(1).uppercase(), 64f, baseline, paint)
        }

        return Icon.createWithBitmap(bitmap)
    }

    private fun callerLabel(callerName: String?, callerNumber: String): String =
        callerName?.trim()?.takeIf(String::isNotEmpty)
            ?: callerNumber.trim().takeIf(String::isNotEmpty)
            ?: context.getString(R.string.anonymous)

    private fun removeCallNotification() {
        @Suppress("DEPRECATION")
        callService?.stopForeground(true)
    }

    private enum class CallType { INCOMING, ONGOING, OUTGOING }

    @AndroidEntryPoint
    class CallButtonsListener : BroadcastReceiver() {
        @Inject
        lateinit var callManager: CallManager

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            val mainCall = callManager.displayState.value.primary ?: return

            if (action == INTENT_ACTION_CALL_BUTTON_CLICK_ACCEPT) {
                callManager.answer(mainCall)
                InCallActivity.start(context)
            } else {
                callManager.hangup(mainCall)
            }
        }
    }
}
