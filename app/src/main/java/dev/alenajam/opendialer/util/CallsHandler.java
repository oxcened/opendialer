package dev.alenajam.opendialer.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telecom.Call;
import android.telecom.CallAudioState;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import dev.alenajam.opendialer.features.inCall.InCallActivity;
import dev.alenajam.opendialer.helper.NotificationHelper;
import dev.alenajam.opendialer.helper.OngoingCallHelper;
import dev.alenajam.opendialer.helper.ProximitySensor;
import dev.alenajam.opendialer.model.OngoingCall;
import dev.alenajam.opendialer.service.InCallServiceImpl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dev.alenajam.opendialer.features.inCall.InCallActivity;

@Singleton
public class CallsHandler {
  private static final MutableLiveData<Map<Call, OngoingCall>> calls = new MutableLiveData<>(new HashMap<>());
  private static final MutableLiveData<OngoingCall> primaryCall = new MutableLiveData<>();
  private static final MutableLiveData<OngoingCall> secondaryCall = new MutableLiveData<>();
  private static final MutableLiveData<CallAudioState> audioState = new MutableLiveData<>();
  private static final MutableLiveData<Boolean> canAddCall = new MutableLiveData<>();
  private static final MutableLiveData<BigDecimal> currentCoins = new MutableLiveData<>();

  @SuppressLint("StaticFieldLeak")
  private static CallsHandler instance;
  private static InCallServiceImpl callService;
  private static InCallActivity inCallActivity;

  private Context context;
  private ProximitySensor proximitySensor;
  private final Observer<Map<Call, OngoingCall>> callsObserver = ongoingCalls -> updateCalls();
  private final Observer<CallAudioState> audioStateObserver = audioState -> updateAudioState();


  @Inject
  public CallsHandler() {
  }

  public static CallsHandler getInstance() {
    if (instance == null) {
      instance = new CallsHandler();
    }

    return instance;
  }

  public static void setInCallActivity(InCallActivity inCallActivity) {
    CallsHandler.inCallActivity = inCallActivity;
  }

  public static void clearInCallActivity(InCallActivity inCallActivity) {
    if (inCallActivity == CallsHandler.inCallActivity) {
      CallsHandler.inCallActivity = null;
    }
  }

  public static boolean isActivityStarted() {
    return inCallActivity != null && !inCallActivity.isDestroyed() && !inCallActivity.isFinishing();
  }

  public static boolean isActivityShowing() {
    if (!isActivityStarted()) return false;

    return inCallActivity.getVisibility();
  }

  public void addCall(Call call, Context context) {
    if (call.getState() == Call.STATE_DISCONNECTED) {
      OngoingCallHelper.handleDisconnectCause(context, call);
      return;
    }

    OngoingCall ongoingCall = new OngoingCall(context, call);

    Map<Call, OngoingCall> map = calls.getValue();
    map.put(call, ongoingCall);

    calls.postValue(map);
  }

  public void removeCall(Call call) {
    if (calls.getValue() == null) return;

    Map<Call, OngoingCall> map = calls.getValue();

    OngoingCall ongoingCall = map.remove(call);
    if (ongoingCall == null) return;

    ongoingCall.tearDown();

    calls.postValue(map);
  }

  public void updateCalls() {
    Map<Call, OngoingCall> map = calls.getValue();
    // finish activity if there are no calls
    if (map.isEmpty()) {
      attemptFinishActivity();
      NotificationHelper.tearDown(callService);
    }

    // set primary and secondary calls
    OngoingCall primary = getPrimaryCallToDisplay();
    if (primary == null) {
      primaryCall.postValue(OngoingCall.ONGOING_CALL_NULL);
    } else {
      primaryCall.postValue(primary);
      handleCallNotification(primary, primary.getState());
      if (primary.getState() == Call.STATE_DIALING) attemptStartActivity();
      updateProximitySensor(primary);

      OngoingCall secondary = getCallToDisplay(primary);
      if (secondary == null) secondaryCall.postValue(OngoingCall.ONGOING_CALL_NULL);
      else secondaryCall.postValue(secondary);
    }
  }

  private void handleCallNotification(OngoingCall call, int state) {
    if (callService == null) return;

    switch (state) {
      case Call.STATE_RINGING:
        if (!CallsHandler.isActivityShowing())
          NotificationHelper.notifyIncomingCall(context, callService, call.getCallerName());
        break;
      case Call.STATE_DIALING:
        NotificationHelper.notifyOutgoingCall(context, callService, call.getCallerName());
        break;
      case Call.STATE_ACTIVE:
        NotificationHelper.notifyOngoingCall(context, callService, call.getCallerName());
        break;
      case Call.STATE_HOLDING:
        NotificationHelper.notifyOnHoldCall(context, callService, call.getCallerName());
        break;
    }
  }

  private void updateAudioState() {
    updateProximitySensor(null);
  }

  private void updateProximitySensor(OngoingCall pCall) {
    OngoingCall call = pCall;
    if (call == null || call == OngoingCall.ONGOING_CALL_NULL) {
      call = primaryCall.getValue();
    }

    if (proximitySensor == null
        || call == null
        || call == OngoingCall.ONGOING_CALL_NULL
        || audioState.getValue() == null)
      return;

    int state = call.getState();
    int audioRoute = audioState.getValue().getRoute();
    proximitySensor.updateProximitySensorMode(state, audioRoute);
  }

  private OngoingCall getPrimaryCallToDisplay() {
    if (getFirstRingingCall() != null) return getFirstRingingCall();
    else if (getFirstDialingCall() != null) return getFirstDialingCall();
    else if (getFirstConnectingCall() != null) return getFirstConnectingCall();
    else if (getCallToDisplay(null) != null) return getCallToDisplay(null);
    return null;
  }

  private OngoingCall getCallToDisplay(OngoingCall ignore) {
    if (getFirstActiveCall() != null && getFirstActiveCall() != ignore)
      return getFirstActiveCall();
    else if (getFirstHoldingCall() != null && getFirstHoldingCall() != ignore)
      return getFirstHoldingCall();
    else if (getSecondHoldingCall() != null && getSecondHoldingCall() != ignore)
      return getSecondHoldingCall();
    else if (getFirstDisconnectingCall() != null && getFirstDisconnectingCall() != ignore)
      return getFirstDisconnectingCall();
    else if (getFirstDisconnectedCall() != null && getFirstDisconnectedCall() != ignore)
      return getFirstDisconnectedCall();
    return null;
  }

  private OngoingCall getFirstConnectingCall() {
    return getFirstCallWithState(Call.STATE_CONNECTING);
  }

  private OngoingCall getFirstDialingCall() {
    return getFirstCallWithState(Call.STATE_DIALING);
  }

  private OngoingCall getFirstRingingCall() {
    return getFirstCallWithState(Call.STATE_RINGING);
  }

  private OngoingCall getFirstActiveCall() {
    return getFirstCallWithState(Call.STATE_ACTIVE);
  }

  private OngoingCall getFirstHoldingCall() {
    return getFirstCallWithState(Call.STATE_HOLDING);
  }

  private OngoingCall getSecondHoldingCall() {
    return getSecondCallWithState(Call.STATE_HOLDING);
  }

  private OngoingCall getFirstDisconnectingCall() {
    return getFirstCallWithState(Call.STATE_DISCONNECTING);
  }

  private OngoingCall getFirstDisconnectedCall() {
    return getFirstCallWithState(Call.STATE_DISCONNECTED);
  }

  @Nullable
  private OngoingCall getFirstCallWithState(int state) {
    if (calls.getValue() == null) return null;

    for (OngoingCall current : calls.getValue().values()) {
      if (current.getState() == state && !current.isConferenced()) {
        return current;
      }
    }

    return null;
  }

  @Nullable
  private OngoingCall getSecondCallWithState(int state) {
    if (calls.getValue() == null) return null;
    int count = 0;

    for (OngoingCall current : calls.getValue().values()) {
      if (current.getState() == state && !current.isConferenced()) {
        if (count == 0) count++;
        else return current;
      }
    }

    return null;
  }

  public void attemptFinishActivity() {
    if (isActivityStarted()) {
      inCallActivity.finish();
    }
  }

  public void attemptStartActivity() {
    if (!isActivityShowing() && context != null) {
      InCallActivity.Companion.start(context);
    }
  }

  public void updateCallAudioState(CallAudioState newAudioState) {
    audioState.postValue(newAudioState);
  }

  public void updateCanAddCall(boolean newCanAddCall) {
    canAddCall.postValue(newCanAddCall);
  }


  public void setup(InCallServiceImpl callService, Context context, ProximitySensor proximitySensor) {
    CallsHandler.callService = callService;
    this.context = context;
    this.proximitySensor = proximitySensor;
    calls.observeForever(callsObserver);
    audioState.observeForever(audioStateObserver);
  }

  public void tearDown() {
    callService = null;
    context = null;
    if (proximitySensor != null) proximitySensor.tearDown();
    proximitySensor = null;
    calls.removeObserver(callsObserver);
    audioState.removeObserver(audioStateObserver);
  }

  public MutableLiveData<CallAudioState> getAudioState() {
    return audioState;
  }

  public MutableLiveData<Boolean> getCanAddCall() {
    return canAddCall;
  }

  public MutableLiveData<OngoingCall> getPrimaryCall() {
    return primaryCall;
  }

  public MutableLiveData<OngoingCall> getSecondaryCall() {
    return secondaryCall;
  }

  public MutableLiveData<Map<Call, OngoingCall>> getCalls() {
    return calls;
  }

  public MutableLiveData<BigDecimal> getCurrentCoins() {
    return currentCoins;
  }
}
