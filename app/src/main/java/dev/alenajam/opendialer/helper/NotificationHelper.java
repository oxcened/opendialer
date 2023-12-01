package dev.alenajam.opendialer.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import dev.alenajam.opendialer.R;

public abstract class NotificationHelper {
  private static final String CHANNEL_ID_INCOMING_CALLS = "dev.alenajam.opendialer.notification_channel.incoming_calls";
  private static final String CHANNEL_ID_ONGOING_CALLS = "dev.alenajam.opendialer.notification_channel.ongoing_calls";
  private static final String CHANNEL_ID_OUTGOING_CALLS = "dev.alenajam.opendialer.notification_channel.outgoing_calls";
  private static final int NOTIFICATION_ID_CALL = 1;
  private static final String INTENT_ACTION_CALL_BUTTON_CLICK_ACCEPT = "dev.alenajam.opendialer.CALL_ACCEPT";
  private static final String INTENT_ACTION_CALL_BUTTON_CLICK_DECLINE = "dev.alenajam.opendialer.CALL_DECLINE";


  public static void setupNotificationChannels(Context context) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      createCallChannel(context, CHANNEL_ID_INCOMING_CALLS, context.getString(R.string.channel_incoming_calls), NotificationManager.IMPORTANCE_HIGH);
      createCallChannel(context, CHANNEL_ID_ONGOING_CALLS, context.getString(R.string.channel_ongoing_calls), NotificationManager.IMPORTANCE_DEFAULT);
      createCallChannel(context, CHANNEL_ID_OUTGOING_CALLS, context.getString(R.string.channel_outgoing_calls), NotificationManager.IMPORTANCE_DEFAULT);
    }
  }

  private static void createCallChannel(Context context, String channelId, String channelName, int channelImportance) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, channelImportance);

      notificationChannel.setSound(null, null);

      NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
      if (notificationManager != null) {
        notificationManager.createNotificationChannel(notificationChannel);
      }
    }
  }
}