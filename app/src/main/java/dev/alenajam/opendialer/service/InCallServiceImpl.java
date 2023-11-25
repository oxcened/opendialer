package dev.alenajam.opendialer.service;

import android.content.Intent;
import android.os.IBinder;
import android.telecom.Call;
import android.telecom.CallAudioState;
import android.telecom.InCallService;

import dev.alenajam.opendialer.helper.ProximitySensor;
import dev.alenajam.opendialer.util.CallsHandler;
import dev.alenajam.opendialer.util.TelecomAdapter;

public class InCallServiceImpl extends InCallService {
  private final TelecomAdapter telecomAdapter = TelecomAdapter.INSTANCE;
  CallsHandler callHandler = CallsHandler.getInstance();

  @Override
  public void onCallAdded(Call call) {
    super.onCallAdded(call);
    callHandler.addCall(call, this);
  }

  @Override
  public void onCallRemoved(Call call) {
    super.onCallRemoved(call);
    callHandler.removeCall(call);
  }

  @Override
  public void onCallAudioStateChanged(CallAudioState audioState) {
    super.onCallAudioStateChanged(audioState);
    callHandler.updateCallAudioState(audioState);
  }

  @Override
  public void onCanAddCallChanged(boolean canAddCall) {
    super.onCanAddCallChanged(canAddCall);
    callHandler.updateCanAddCall(canAddCall);
  }

  @Override
  public void onBringToForeground(boolean showDialpad) {
    super.onBringToForeground(showDialpad);
    callHandler.attemptStartActivity();
  }

  @Override
  public IBinder onBind(Intent intent) {
    callHandler.setup(
        this,
        getApplicationContext(),
        new ProximitySensor(getApplicationContext())
    );
    telecomAdapter.setCallService(this);

    return super.onBind(intent);
  }

  @Override
  public boolean onUnbind(Intent intent) {
    callHandler.tearDown();
    telecomAdapter.tearDown();

    return super.onUnbind(intent);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }
}
