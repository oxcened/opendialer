package dev.alenajam.opendialer.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;
import android.telecom.Call;
import android.telecom.CallAudioState;
import android.util.Log;

import androidx.annotation.NonNull;

public class ProximitySensor {
  private static final String TAG = ProximitySensor.class.getSimpleName();

  private final PowerManager.WakeLock proximityWakeLock;

  public ProximitySensor(@NonNull Context context) {
    PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    if (powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
      proximityWakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, TAG);
    } else {
      Log.i(TAG, "Device does not support proximity wake lock.");
      proximityWakeLock = null;
    }
  }

  public void updateProximitySensorMode(int state, int audioRoute) {
    boolean on = false;

    switch (state) {
      case Call.STATE_CONNECTING:
      case Call.STATE_DIALING:
      case Call.STATE_ACTIVE:
        on = audioRoute == CallAudioState.ROUTE_EARPIECE;
    }

    if (on) turnOnProximitySensor();
    else turnOffProximitySensor(true);
  }

  @SuppressLint("WakelockTimeout")
  private void turnOnProximitySensor() {
    if (proximityWakeLock != null) {
      if (!proximityWakeLock.isHeld()) {
        proximityWakeLock.acquire();
      }
    }
  }

  private void turnOffProximitySensor(boolean screenOnImmediately) {
    if (proximityWakeLock != null) {
      if (proximityWakeLock.isHeld()) {
        int flags = (screenOnImmediately ? 0 : PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY);
        proximityWakeLock.release(flags);
      }
    }
  }

  public void tearDown() {
    turnOffProximitySensor(true);
  }
}