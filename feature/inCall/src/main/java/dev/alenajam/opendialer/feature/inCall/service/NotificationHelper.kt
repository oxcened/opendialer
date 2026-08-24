package dev.alenajam.opendialer.feature.inCall.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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

    @JvmStatic
    fun setupNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createCallChannel(
                context,
                CHANNEL_ID_INCOMING_CALLS,
                context.getString(R.string.channel_incoming_calls),
                NotificationManager.IMPORTANCE_HIGH
            )
            createCallChannel(
                context,
                CHANNEL_ID_ONGOING_CALLS,
                context.getString(R.string.channel_ongoing_calls),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            createCallChannel(
                context,
                CHANNEL_ID_OUTGOING_CALLS,
                context.getString(R.string.channel_outgoing_calls),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        }
    }

    private fun createCallChannel(
        context: Context,
        channelId: String,
        channelName: String,
        channelImportance: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, channelImportance)
            notificationChannel.setSound(null, null)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    private fun notifyCall(
        context: Context,
        callService: InCallServiceImpl?,
        channelId: String,
        priority: Int,
        notificationText: String,
        fullScreen: Boolean
    ): Notification? {
        setupNotificationChannels(context)

        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            flags = Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT
            setClass(context, InCallActivity::class.java)
        }
        val pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, channelId)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }

        @Suppress("DEPRECATION")
        builder.setPriority(priority)
        builder.setContentIntent(pendingIntent)
        builder.setContentTitle(notificationText)
        builder.setContentText(notificationText)
        if (fullScreen) builder.setFullScreenIntent(pendingIntent, true)
        builder.setSmallIcon(R.drawable.ic_notification_call)
        builder.setCategory(Notification.CATEGORY_CALL)
        builder.setVisibility(Notification.VISIBILITY_PUBLIC)
        builder.setOngoing(true)

        if (callService != null) {
            val notification = builder.build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                callService.startForeground(
                    NOTIFICATION_ID_CALL,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
                )
            } else {
                callService.startForeground(NOTIFICATION_ID_CALL, notification)
            }
            return notification
        }
        return null
    }

    @JvmStatic
    fun notifyIncomingCall(context: Context, callService: InCallServiceImpl, caller: String): Notification? {
        @Suppress("DEPRECATION")
        return notifyCall(
            context,
            callService,
            CHANNEL_ID_INCOMING_CALLS,
            Notification.PRIORITY_MAX,
            context.getString(R.string.notification_incoming_call_title, caller),
            true
        )
    }

    @JvmStatic
    fun notifyOutgoingCall(context: Context, callService: InCallServiceImpl, caller: String) {
        @Suppress("DEPRECATION")
        notifyCall(
            context,
            callService,
            CHANNEL_ID_OUTGOING_CALLS,
            Notification.PRIORITY_DEFAULT,
            context.getString(R.string.notification_outgoing_call_title, caller),
            false
        )
    }

    @JvmStatic
    fun notifyOngoingCall(context: Context, callService: InCallServiceImpl, caller: String) {
        @Suppress("DEPRECATION")
        notifyCall(
            context,
            callService,
            CHANNEL_ID_ONGOING_CALLS,
            Notification.PRIORITY_DEFAULT,
            context.getString(R.string.notification_ongoing_call_title, caller),
            false
        )
    }

    @JvmStatic
    fun notifyOnHoldCall(context: Context, callService: InCallServiceImpl, caller: String) {
        @Suppress("DEPRECATION")
        notifyCall(
            context,
            callService,
            CHANNEL_ID_ONGOING_CALLS,
            Notification.PRIORITY_DEFAULT,
            context.getString(R.string.notification_on_hold_call_title, caller),
            false
        )
    }

    @JvmStatic
    fun notifyDisconnectingCall(context: Context, callService: InCallServiceImpl, caller: String) {
        @Suppress("DEPRECATION")
        notifyCall(
            context,
            callService,
            CHANNEL_ID_ONGOING_CALLS,
            Notification.PRIORITY_DEFAULT,
            context.getString(R.string.notification_disconnecting_call_title, caller),
            false
        )
    }

    @JvmStatic
    fun removeCallNotification(callService: InCallServiceImpl?) {
        @Suppress("DEPRECATION")
        callService?.stopForeground(true)
    }

    @JvmStatic
    fun tearDown(callService: InCallServiceImpl?) {
        removeCallNotification(callService)
    }

    @AndroidEntryPoint
    class CallButtonsListener : BroadcastReceiver() {
        @Inject
        lateinit var callHandler: CallsHandler

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return

            val displayState = callHandler.displayState.value
            val mainCall = displayState?.primary ?: return

            if (action == INTENT_ACTION_CALL_BUTTON_CLICK_ACCEPT) {
                mainCall.answer()
                callHandler.attemptStartActivity()
            } else {
                mainCall.hangup()
            }
        }
    }
}
