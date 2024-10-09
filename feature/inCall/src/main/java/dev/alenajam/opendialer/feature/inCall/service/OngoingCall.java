package dev.alenajam.opendialer.feature.inCall.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.telecom.Call;
import android.telecom.VideoProfile;

import java.util.List;

import javax.annotation.Nullable;

import dev.alenajam.opendialer.core.common.CommonUtils;
import dev.alenajam.opendialer.core.common.Contact;
import dev.alenajam.opendialer.core.common.ContactsHelper;

public class OngoingCall {
  private static final int DTMF_DURATION_MS = 300;
  @SuppressLint("StaticFieldLeak")
  public static OngoingCall ONGOING_CALL_NULL = new OngoingCall();
  private Call call;
  private String callerNumber = "", keypadText = "", callerName, callerImageUri = null;
  private long startTime = -1, totalTime = 0;
  private int type;
  private Context context;
  private final CallsHandler callsHandler = CallsHandler.getInstance();

  private final Call.Callback callback = new Call.Callback() {
    @Override
    public void onStateChanged(Call call, int newState) {
      super.onStateChanged(call, newState);
      updateState(newState);
      callsHandler.updateCalls();
    }

    @Override
    public void onConferenceableCallsChanged(Call call, List<Call> conferenceableCalls) {
      super.onConferenceableCallsChanged(call, conferenceableCalls);
      callsHandler.updateCalls();
    }

    @Override
    public void onParentChanged(Call call, Call parent) {
      super.onParentChanged(call, parent);
      callsHandler.updateCalls();
    }

    @Override
    public void onDetailsChanged(Call call, Call.Details details) {
      super.onDetailsChanged(call, details);
      callsHandler.updateCalls();
    }
  };

  public OngoingCall() {
  }

  public OngoingCall(Context context, Call call) {
    this.call = call;
    this.context = context;

    init();
  }

  private void init() {
    call.registerCallback(callback);

    type = getState() == Call.STATE_RINGING ? OngoingCallHelper.CALL_TYPE_INCOMING : OngoingCallHelper.CALL_TYPE_OUTGOING;

    if (isConference()) {
      //callerName = context.getString(R.string.conferenceCall);
    } else if (isAnonymous()) {
      // callerName = context.getString(R.string.anonymous);
    } else {
      Uri numberUri = call.getDetails().getHandle();
      callerNumber = numberUri.getSchemeSpecificPart();
      Contact savedContact = ContactsHelper.getContactByPhoneNumber(context, callerNumber);

      if (savedContact != null) {
        callerName = savedContact.getName();
        callerImageUri = savedContact.getImageUri();
      } else {
        callerName = callerNumber;
      }
    }

    updateState(getState());
  }

  public void tearDown() {
    call.unregisterCallback(callback);
  }

  public void updateState(int state) {
    handleCall(state);
  }

  private void handleCall(int state) {
    switch (state) {
      case Call.STATE_HOLDING:
        long totalTime = CommonUtils.getCurrentTime() - getStartTime();
        totalTime += getTotalTime();
        this.totalTime = totalTime;
        break;
      case Call.STATE_DISCONNECTED:
        CallsHandler.getInstance().removeCall(call);
        OngoingCallHelper.handleDisconnectCause(context, call);
        break;
      case Call.STATE_ACTIVE:
        this.startTime = CommonUtils.getCurrentTime();
        break;
    }
  }

  public Call getCall() {
    return call;
  }

  public void setCall(Call call) {
    this.call = call;
  }

  public String getCallerNumber() {
    return callerNumber;
  }

  @Nullable
  public String getCallerName() {
    return callerName;
  }

  @Nullable
  public String getCallerImageUri() {
    return callerImageUri;
  }

  public String getKeypadText() {
    return keypadText;
  }

  public void setKeypadText(String keypadText) {
    this.keypadText = keypadText;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getTotalTime() {
    return totalTime;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public Integer getState() {
    if (call == null) return null;
    return call.getState();
  }

  public void answer() {
    if (call == null) return;
    call.answer(VideoProfile.STATE_AUDIO_ONLY);
  }

  public void hangup() {
    if (call == null) return;
    if (getState() == Call.STATE_RINGING) {
      call.reject(false, null);
    } else {
      call.disconnect();
    }
  }

  public void hangup(String message) {
    if (call == null) return;
    if (getState() == Call.STATE_RINGING) {
      call.reject(true, message);
    } else {
      call.disconnect();
    }
  }

  public void hold() {
    if (call == null) return;
    if (getState() == Call.STATE_HOLDING) {
      call.unhold();
    } else {
      call.hold();
    }
  }

  public void hold(boolean hold) {
    if (call == null) return;
    if (hold) call.hold();
    else call.unhold();
  }

  public void playDtmf(char digit) {
    if (call == null) return;
    call.playDtmfTone(digit);
    new Handler(Looper.getMainLooper()).postDelayed(call::stopDtmfTone, DTMF_DURATION_MS);
  }

  public boolean isAnonymous() {
    if (call == null) return false;
    return call.getDetails().getHandle() == null;
  }

  public boolean canBeMerged() {
    if (call == null) return false;

    if (call.getDetails().can(Call.Details.CAPABILITY_MERGE_CONFERENCE)) return true;

    if (call.getConferenceableCalls().size() > 0) return true;

    return false;
  }

  public boolean isConference() {
    if (call == null) return false;

    return call.getDetails().hasProperty(Call.Details.PROPERTY_CONFERENCE);
  }

  public boolean isConferenced() {
    return call != null && call.getParent() != null;
  }
}
