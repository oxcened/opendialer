package dev.alenajam.opendialer.core.common;

import static android.content.Context.TELECOM_SERVICE;
import static android.content.Context.TELEPHONY_SUBSCRIPTION_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioManager;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class CommonUtils {

  @SuppressLint("DefaultLocale")
  public static String getDurationTimeString(long durationMilliseconds) {

    return String.format("%02d:%02d:%02d",
        TimeUnit.MILLISECONDS.toHours(durationMilliseconds),
        TimeUnit.MILLISECONDS.toMinutes(durationMilliseconds) -
            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(durationMilliseconds)),
        TimeUnit.MILLISECONDS.toSeconds(durationMilliseconds) -
            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationMilliseconds))
    );
  }

  @SuppressLint("DefaultLocale")
  public static String getDurationTimeStringMinimal(long durationMilliseconds) {
    long hours = TimeUnit.MILLISECONDS.toHours(durationMilliseconds);
    long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMilliseconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(durationMilliseconds));
    long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMilliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationMilliseconds));

    String timeString = "";
    if (hours > 0) timeString = timeString.concat(hours + "h ");
    if (minutes > 0) timeString = timeString.concat(minutes + "m ");
    if (seconds > 0) timeString = timeString.concat(seconds + "s");

    return timeString.isEmpty() ? "0s" : timeString;
  }

  public static Bitmap textToBitmap(Context context, String messageText, float textSize, int textColor) {
    Paint paint = new Paint();
    int pixelTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        textSize, context.getResources().getDisplayMetrics());
    paint.setTextSize(pixelTextSize);
    paint.setColor(textColor);
    paint.setTextAlign(Paint.Align.LEFT);

    float baseline = -paint.ascent() + 10; // ascent() is negative
    int width = (int) (paint.measureText(messageText) + 0.5f); // round
    int height = (int) (baseline + paint.descent() + 0.5f);

    Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

    Canvas canvas = new Canvas(image);
    canvas.drawText(messageText, 0, baseline, paint);

    return image;
  }

  public static String getEditTextSelectedText(EditText editText) {
    if (!editText.hasSelection()) return null;

    final int start = editText.getSelectionStart();
    final int end = editText.getSelectionEnd();

    return String.valueOf(
        start > end ? editText.getText().subSequence(end, start) : editText.getText().subSequence(start, end));
  }

  @Deprecated
  public static void startInCallUI(Context context) throws ClassNotFoundException {
    Intent intent = new Intent(context, Class.forName("dev.alenajam.opendialer.features.inCall.ui.InCallActivity"));
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }

  @SuppressLint("MissingPermission")
  public static void makeCall(Context context, String number) {
    if (PermissionUtils.hasMakeCallPermission(context)) {
      Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", number, null));

      TelecomManager telecomManager = (TelecomManager) context.getSystemService(TELECOM_SERVICE);
      if (telecomManager == null) return;
      PhoneAccountHandle defaultPhoneAccount = telecomManager.getDefaultOutgoingPhoneAccount("tel");
      if (defaultPhoneAccount == null) {
        List<PhoneAccountHandle> phoneAccounts = telecomManager.getCallCapablePhoneAccounts();
        if (phoneAccounts.size() < 2) return;
        else {
          // Dual SIM
          SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE);
          if (subscriptionManager == null) return;

          MyDialog dialog = new MyDialog(context);
          dialog.setTitle(context.getString(R.string.choose_sim_card));
          LinearLayout linearLayout = new LinearLayout(context);
          linearLayout.setOrientation(LinearLayout.VERTICAL);
          for (int i = 0; i < phoneAccounts.size(); i++) {
            SubscriptionInfo subscriptionInfo = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(i);
            if (subscriptionInfo == null) continue;

            View item = LayoutInflater.from(context).inflate(R.layout.item_sim, null);
            TextView carrier = item.findViewById(R.id.carrier);
            carrier.setText(subscriptionInfo.getCarrierName());
            TextView numberTv = item.findViewById(R.id.number);
            numberTv.setText(subscriptionInfo.getNumber());
            subscriptionInfo.getIconTint();
            ImageView icon = item.findViewById(R.id.icon);
            icon.setColorFilter(subscriptionInfo.getIconTint());
            int finalI = i;
            item.setOnClickListener(v -> {
              dialog.hide();
              intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccounts.get(finalI));
              context.startActivity(intent);
            });
            linearLayout.addView(item);
          }
          dialog.setContent(linearLayout);
          dialog.show();
          return;
        }
      }
      context.startActivity(intent);
    }
  }

  public static void makeSms(Context context, String number) {
    context.startActivity(new Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", number, null)));
  }

  public static void copyToClipobard(Context context, String text) {
    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newPlainText(null, text);
    if (clipboard != null) {
      clipboard.setPrimaryClip(clip);
      Toast.makeText(context, context.getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
    }
  }

  public static void showContactDetail(Context context, int contactId) {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contactId));
    intent.setData(uri);
    context.startActivity(intent);
  }

  public static void setTheme(int mode) {
    AppCompatDelegate.setDefaultNightMode(mode);
  }

  public static void hideKeyboard(Activity activity) {
    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
    //Find the currently focused view, so we can grab the correct window token from it.
    View view = activity.getCurrentFocus();
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
      view = new View(activity);
    }
    if (imm != null) {
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

  public static float convertDpToPixels(float dp, Context context) {
    return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
  }

  public static void addContactAsExisting(Context context, String number) {
    Intent addExistingIntent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
    addExistingIntent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
    addExistingIntent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
    context.startActivity(addExistingIntent);
  }

  public static void createContact(Context context, String number) {
    Intent createContactIntent = new Intent(Intent.ACTION_INSERT);
    createContactIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
    createContactIntent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
    context.startActivity(createContactIntent);
  }

  public static boolean isDmtfSettingEnabled(Context context) {
    return Settings.System.getInt(context.getContentResolver(), Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;
  }

  public static boolean isSoundEffectsEnabled(Context context) {
    return Settings.System.getInt(context.getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 1) == 1;
  }

  public static boolean isRingerModeSilentOrVibrate(Context context) {
    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    if (audioManager == null) return false;
    int ringerMode = audioManager.getRingerMode();
    return ringerMode == AudioManager.RINGER_MODE_SILENT || ringerMode == AudioManager.RINGER_MODE_VIBRATE;
  }

  public static int getColorFromAttr(Context context, int attrInt) {
    TypedValue typedValue = new TypedValue();
    context.getTheme().resolveAttribute(attrInt, typedValue, true);
    return ContextCompat.getColor(context, typedValue.resourceId);
  }

  public static long getCurrentTime() {
    return SystemClock.elapsedRealtime();
  }
}