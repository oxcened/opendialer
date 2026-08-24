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
import dagger.hilt.android.AndroidEntryPoint
import dev.alenajam.opendialer.feature.inCall.R
import dev.alenajam.opendialer.feature.inCall.ui.InCallActivity
import javax.inject.Inject

object NotificationHelper {
    private const val CHANNEL_ID_INCOMING_CALLS = "dev.alenajam.opendialer.notification_channel.incoming_calls"
    private const val CHANNEL_ID_ONGOING_CALLS = "dev.alenajam.opendialer.notification_channel.ongoing_calls"
    private const val CHANNEL_ID_OUTGOING_CALLS = "dev.alenajam.opendialer.notification_channel.outgoing_calls"
    private const val NOTIFICATION_ID_CALL = 1
    private const val INTENT_ACTION_CALL_BUTTON_CLICK_ACCEPT = "dev.alenajam.opendialer.CALL_ACCEPT"
    private const val INTENT_ACTION_CALL_BUTTON_CLICK_DECLINE = "dev.alenajam.opendialer.CALL_DECLINE"

    fun setupNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java) ?: return

            val channels = listOf(
                NotificationChannel(CHANNEL_ID_INCOMING_CALLS, context.getString(R.string.channel_incoming_calls), NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_ID_ONGOING_CALLS, context.getString(R.string.channel_ongoing_calls), NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(CHANNEL_ID_OUTGOING_CALLS, context.getString(R.string.channel_outgoing_calls), NotificationManager.IMPORTANCE_DEFAULT)
            )

            channels.forEach { channel ->
                channel.setSound(null, null)
                nm.createNotificationChannel(channel)
            }
        }
    }

    private fun notifyCall(
        context: Context,
        callService: InCallServiceImpl?,
        channelId: String,
        caller: String,
        type: CallType
    ): Notification? {
        setupNotificationChannels(context)

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
            val person = Person.Builder()
                .setName(caller)
                .setImportant(true)
                .build()
            val style = when (type) {
                CallType.INCOMING -> Notification.CallStyle.forIncomingCall(person, getDeclineIntent(context), getAcceptIntent(context))
                CallType.ONGOING -> Notification.CallStyle.forOngoingCall(person, getDeclineIntent(context))
                CallType.OUTGOING -> Notification.CallStyle.forOngoingCall(person, getDeclineIntent(context))
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
                builder.addAction(Notification.Action.Builder(null, "Decline", getDeclineIntent(context)).build())
                builder.addAction(Notification.Action.Builder(null, "Answer", getAcceptIntent(context)).build())
                builder.setFullScreenIntent(pendingIntent, true)
            }
        }

        if (callService != null) {
            val notification = builder.build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                callService.startForeground(NOTIFICATION_ID_CALL, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)
            } else {
                callService.startForeground(NOTIFICATION_ID_CALL, notification)
            }
            return notification
        }
        return null
    }

    private fun getAcceptIntent(context: Context): PendingIntent {
        val intent = Intent(context, CallButtonsListener::class.java).apply {
            action = INTENT_ACTION_CALL_BUTTON_CLICK_ACCEPT
        }
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun getDeclineIntent(context: Context): PendingIntent {
        val intent = Intent(context, CallButtonsListener::class.java).apply {
            action = INTENT_ACTION_CALL_BUTTON_CLICK_DECLINE
        }
        return PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    fun notifyIncomingCall(context: Context, callService: InCallServiceImpl, caller: String) =
        notifyCall(context, callService, CHANNEL_ID_INCOMING_CALLS, caller, CallType.INCOMING)

    fun notifyOutgoingCall(context: Context, callService: InCallServiceImpl, caller: String) =
        notifyCall(context, callService, CHANNEL_ID_OUTGOING_CALLS, caller, CallType.OUTGOING)

    fun notifyOngoingCall(context: Context, callService: InCallServiceImpl, caller: String) =
        notifyCall(context, callService, CHANNEL_ID_ONGOING_CALLS, caller, CallType.ONGOING)

    fun notifyOnHoldCall(context: Context, callService: InCallServiceImpl, caller: String) =
        notifyCall(context, callService, CHANNEL_ID_ONGOING_CALLS, caller, CallType.ONGOING)

    fun notifyDisconnectingCall(context: Context, callService: InCallServiceImpl, caller: String) =
        notifyCall(context, callService, CHANNEL_ID_ONGOING_CALLS, caller, CallType.ONGOING)

    fun removeCallNotification(callService: InCallServiceImpl?) {
        @Suppress("DEPRECATION")
        callService?.stopForeground(true)
    }

    fun tearDown(callService: InCallServiceImpl?) {
        removeCallNotification(callService)
    }

    private enum class CallType { INCOMING, ONGOING, OUTGOING }

    @AndroidEntryPoint
    class CallButtonsListener : BroadcastReceiver() {
        @Inject
        lateinit var callHandler: CallsHandler

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            val mainCall = callHandler.displayState.value.primary ?: return

            if (action == INTENT_ACTION_CALL_BUTTON_CLICK_ACCEPT) {
                mainCall.answer()
                callHandler.attemptStartActivity()
            } else {
                mainCall.hangup()
            }
        }
    }
}
