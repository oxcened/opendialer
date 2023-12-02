package dev.alenajam.opendialer.feature.inCall.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import dev.alenajam.opendialer.feature.inCall.R;
import dev.alenajam.opendialer.feature.inCall.ui.InCallActivity;

public abstract class NotificationHelper {
  private static final String CHANNEL_ID_INCOMING_CALLS = "dev.alenajam.opendialer.notification_channel.incoming_calls";
  private static final String CHANNEL_ID_ONGOING_CALLS = "dev.alenajam.opendialer.notification_channel.ongoing_calls";
  private static final String CHANNEL_ID_OUTGOING_CALLS = "dev.alenajam.opendialer.notification_channel.outgoing_calls";
  private static final int NOTIFICATION_ID_CALL = 1;
  private static final String INTENT_ACTION_CALL_BUTTON_CLICK_ACCEPT = "dev.alenajam.opendialer.CALL_ACCEPT";
  private static final String INTENT_ACTION_CALL_BUTTON_CLICK_DECLINE = "dev.alenajam.opendialer.CALL_DECLINE";


  public static void setupNotificationChannels(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      createCallChannel(context, CHANNEL_ID_INCOMING_CALLS, context.getString(R.string.channel_incoming_calls), NotificationManager.IMPORTANCE_HIGH);
      createCallChannel(context, CHANNEL_ID_ONGOING_CALLS, context.getString(R.string.channel_ongoing_calls), NotificationManager.IMPORTANCE_DEFAULT);
      createCallChannel(context, CHANNEL_ID_OUTGOING_CALLS, context.getString(R.string.channel_outgoing_calls), NotificationManager.IMPORTANCE_DEFAULT);
    }
  }

  private static void createCallChannel(Context context, String channelId, String channelName, int channelImportance) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, channelImportance);

      notificationChannel.setSound(null, null);

      NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
      if (notificationManager != null) {
        notificationManager.createNotificationChannel(notificationChannel);
      }
    }
  }

  private static Notification notifyCall(Context context, InCallServiceImpl callService, String channelId, int priority, String notificationText) {
    Intent intent = new Intent(Intent.ACTION_MAIN, null);
    intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
    intent.setClass(context, InCallActivity.class);
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_IMMUTABLE);

    final Notification.Builder builder;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      builder = new Notification.Builder(context, channelId);
    } else {
      builder = new Notification.Builder(context);
    }

    builder.setPriority(priority);
    builder.setContentIntent(pendingIntent);
    builder.setFullScreenIntent(pendingIntent, true);
    builder.setSmallIcon(R.drawable.ic_notification_call);

    // TODO rework with native UI instead of RemoteViews
    /*RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.notification_call);
    remoteView.setImageViewBitmap(R.id.text_view, CommonUtils.textToBitmap(context, notificationText.toUpperCase(), 15, Color.BLACK));

    RemoteViews remoteViewHeadsUp = new RemoteViews(context.getPackageName(), R.layout.notification_call_heads_up);
    remoteViewHeadsUp.setImageViewBitmap(R.id.text_view, CommonUtils.textToBitmap(context, notificationText.toUpperCase(), 15, Color.BLACK));

    remoteViewHeadsUp.setImageViewBitmap(R.id.buttonLeft, CommonUtils.textToBitmap(context, context.getString(R.string.fight).toUpperCase(), 15, context.getColor(R.color.green)));
    Intent buttonLeftIntent = new Intent(context, CallButtonsListener.class);
    buttonLeftIntent.setAction(INTENT_ACTION_CALL_BUTTON_CLICK_ACCEPT);
    PendingIntent buttonLeftPendingIntent = PendingIntent.getBroadcast(context, 0, buttonLeftIntent, PendingIntent.FLAG_IMMUTABLE);
    remoteViewHeadsUp.setOnClickPendingIntent(R.id.buttonLeft, buttonLeftPendingIntent);

    remoteViewHeadsUp.setImageViewBitmap(R.id.buttonRight, CommonUtils.textToBitmap(context, context.getString(R.string.run).toUpperCase(), 15, context.getColor(R.color.red)));
    Intent buttonRightIntent = new Intent(context, CallButtonsListener.class);
    buttonRightIntent.setAction(INTENT_ACTION_CALL_BUTTON_CLICK_DECLINE);
    PendingIntent buttonRightPendingIntent = PendingIntent.getBroadcast(context, 0, buttonRightIntent, PendingIntent.FLAG_IMMUTABLE);
    remoteViewHeadsUp.setOnClickPendingIntent(R.id.buttonRight, buttonRightPendingIntent);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      builder.setCustomHeadsUpContentView(remoteViewHeadsUp);
      builder.setCustomContentView(remoteView);
    }*/

    builder.setCategory(Notification.CATEGORY_CALL);

    builder.setVisibility(Notification.VISIBILITY_PUBLIC);

    builder.setOngoing(true);

    if (callService != null) {
      Notification notification = builder.build();
      callService.startForeground(NOTIFICATION_ID_CALL, notification);
      return notification;
    } else return null;
  }

  public static Notification notifyIncomingCall(Context context, InCallServiceImpl callService, String caller) {
    return notifyCall(context, callService, CHANNEL_ID_INCOMING_CALLS, Notification.PRIORITY_MAX, context.getString(R.string.notification_incoming_call_title, caller));
  }

  public static void notifyOutgoingCall(Context context, InCallServiceImpl callService, String caller) {
    notifyCall(context, callService, CHANNEL_ID_OUTGOING_CALLS, Notification.PRIORITY_DEFAULT, context.getString(R.string.notification_outgoing_call_title, caller));
  }

  public static void notifyOngoingCall(Context context, InCallServiceImpl callService, String caller) {
    notifyCall(context, callService, CHANNEL_ID_ONGOING_CALLS, Notification.PRIORITY_DEFAULT, context.getString(R.string.notification_ongoing_call_title, caller));
  }

  public static void notifyOnHoldCall(Context context, InCallServiceImpl callService, String caller) {
    notifyCall(context, callService, CHANNEL_ID_ONGOING_CALLS, Notification.PRIORITY_DEFAULT, context.getString(R.string.notification_on_hold_call_title, caller));
  }

  public static void removeCallNotification(InCallServiceImpl callService) {
    if (callService != null) callService.stopForeground(true);
  }

  public static void tearDown(InCallServiceImpl callService) {
    removeCallNotification(callService);
  }

  public static class CallButtonsListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction() == null) return;

      CallsHandler callHandler = CallsHandler.getInstance();
      OngoingCall mainCall = callHandler.getPrimaryCall().getValue();
      if (mainCall == null) return;

      if (intent.getAction().equals(INTENT_ACTION_CALL_BUTTON_CLICK_ACCEPT)) {
        mainCall.answer();
        CallsHandler.getInstance().attemptStartActivity();
      } else {
        mainCall.hangup();
      }
    }
  }
}