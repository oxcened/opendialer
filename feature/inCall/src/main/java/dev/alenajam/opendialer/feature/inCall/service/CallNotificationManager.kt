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
import android.os.Build
import android.telecom.Call
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
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
        private const val NOTIFICATION_ID_CALL = 1
        private const val INTENT_ACTION_CALL_BUTTON_CLICK_ACCEPT = "dev.alenajam.opendialer.CALL_ACCEPT"
        private const val INTENT_ACTION_CALL_BUTTON_CLICK_DECLINE = "dev.alenajam.opendialer.CALL_DECLINE"
    }

    private var callService: InCallServiceImpl? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observationJob: Job? = null

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
    }

    private fun handleCallNotification(call: OngoingCall, state: Int) {
        val caller = call.callerName ?: call.callerNumber.ifBlank { context.getString(R.string.anonymous) }
        
        when (state) {
            Call.STATE_RINGING -> notifyIncomingCall(caller)
            Call.STATE_DIALING -> notifyOutgoingCall(caller)
            Call.STATE_ACTIVE -> notifyOngoingCall(caller)
            Call.STATE_HOLDING -> notifyOnHoldCall(caller)
            Call.STATE_DISCONNECTING -> notifyDisconnectingCall(caller)
            else -> removeCallNotification()
        }
    }

    private fun setupNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java) ?: return
            val channels = listOf(
                NotificationChannel(CHANNEL_ID_INCOMING_CALLS, context.getString(R.string.channel_incoming_calls), NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_ID_ONGOING_CALLS, context.getString(R.string.channel_ongoing_calls), NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(CHANNEL_ID_OUTGOING_CALLS, context.getString(R.string.channel_outgoing_calls), NotificationManager.IMPORTANCE_DEFAULT)
            )
            channels.forEach { it.setSound(null, null) }
            nm.createNotificationChannels(channels)
        }
    }

    private fun notifyCall(channelId: String, caller: String, type: CallType) {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val person = Person.Builder().setName(caller).setImportant(true).build()
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
                builder.setFullScreenIntent(pendingIntent, true)
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

    private fun notifyIncomingCall(caller: String) = notifyCall(CHANNEL_ID_INCOMING_CALLS, caller, CallType.INCOMING)
    private fun notifyOutgoingCall(caller: String) = notifyCall(CHANNEL_ID_OUTGOING_CALLS, caller, CallType.OUTGOING)
    private fun notifyOngoingCall(caller: String) = notifyCall(CHANNEL_ID_ONGOING_CALLS, caller, CallType.ONGOING)
    private fun notifyOnHoldCall(caller: String) = notifyCall(CHANNEL_ID_ONGOING_CALLS, caller, CallType.ONGOING)
    private fun notifyDisconnectingCall(caller: String) = notifyCall(CHANNEL_ID_ONGOING_CALLS, caller, CallType.ONGOING)

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
