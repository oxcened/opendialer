package dev.alenajam.opendialer.feature.inCall.service;

import android.content.Context;
import android.telecom.Call;
import android.telecom.CallAudioState;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dev.alenajam.opendialer.feature.inCall.ui.InCallActivity;

@Singleton
public class CallsHandler {
    private final MutableLiveData<Map<Call, OngoingCall>> calls = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<CallDisplayState> displayState =
            new MutableLiveData<>(new CallDisplayState(null, null));
    private final MutableLiveData<CallAudioState> audioState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> canAddCall = new MutableLiveData<>();
    private InCallServiceImpl callService;
    private InCallActivity inCallActivity;

    private Context context;
    private ProximitySensor proximitySensor;
    private long nextCallSequence;
    private final Observer<Map<Call, OngoingCall>> callsObserver = ongoingCalls -> updateCalls();
    private final Observer<CallAudioState> audioStateObserver = audioState -> updateAudioState();


    @Inject
    public CallsHandler() {
    }

    public void setInCallActivity(InCallActivity inCallActivity) {
        this.inCallActivity = inCallActivity;
    }

    public void clearInCallActivity(InCallActivity inCallActivity) {
        if (inCallActivity == this.inCallActivity) {
            this.inCallActivity = null;
        }
    }

    public boolean isActivityStarted() {
        return inCallActivity != null && !inCallActivity.isDestroyed() && !inCallActivity.isFinishing();
    }

    public boolean isActivityShowing() {
        if (!isActivityStarted()) return false;

        return inCallActivity.getVisibility();
    }

    public void addCall(Call call, Context context) {
        if (call.getState() == Call.STATE_DISCONNECTED) {
            OngoingCallHelper.handleDisconnectCause(context, call);
            return;
        }

        OngoingCall ongoingCall = new OngoingCall(context, call, this, nextCallSequence++);

        // Copy the map so a new reference is posted. LiveData.postValue with the
        // same mutated-in-place HashMap instance is not detected as a change by
        // Compose's observeAsState (structural equality on the same reference
        // is always true), so conference UI would not recompose on add/remove.
        Map<Call, OngoingCall> map = new HashMap<>(calls.getValue());
        map.put(call, ongoingCall);

        // Telecom and Call callbacks run on the main thread. Publish immediately
        // so simultaneous conference updates cannot overwrite one another.
        calls.setValue(map);
    }

    public void removeCall(Call call) {
        if (calls.getValue() == null) return;

        Map<Call, OngoingCall> map = new HashMap<>(calls.getValue());

        OngoingCall ongoingCall = map.remove(call);
        if (ongoingCall == null) return;

        ongoingCall.tearDown();

        // Conference members can disconnect back-to-back. Using postValue here
        // lets each callback copy the same stale map, so the last posted removal
        // can restore calls removed by earlier callbacks.
        calls.setValue(map);
    }

    public void updateCalls() {
        Map<Call, OngoingCall> map = calls.getValue();
        if (map.isEmpty() && restoreCallsFromTelecom()) return;

        // finish activity if there are no calls
        if (map.isEmpty()) {
            displayState.setValue(new CallDisplayState(null, null));
            attemptFinishActivity();
            NotificationHelper.tearDown(callService);
            return;
        }

        // set primary and secondary calls
        OngoingCall primary = getPrimaryCallToDisplay();
        if (primary == null) {
            displayState.setValue(new CallDisplayState(null, null));
        } else {
            OngoingCall secondary = getCallToDisplay(primary);
            displayState.setValue(new CallDisplayState(primary, secondary));
            handleCallNotification(primary, primary.getState());
            if (primary.getState() == Call.STATE_DIALING) attemptStartActivity();
            updateProximitySensor(primary);
        }
    }

    private boolean restoreCallsFromTelecom() {
        if (callService == null) return false;

        Map<Call, OngoingCall> restoredCalls = new HashMap<>();
        for (Call call : callService.getCalls()) {
            if (call.getState() != Call.STATE_DISCONNECTED) {
                restoredCalls.put(
                        call,
                        new OngoingCall(context, call, this, nextCallSequence++)
                );
            }
        }

        if (restoredCalls.isEmpty()) return false;

        // A conference can survive after its independent companion call is
        // removed even when all local entries were cleared during the handoff.
        calls.setValue(restoredCalls);
        return true;
    }

    private void handleCallNotification(OngoingCall call, int state) {
        if (callService == null) return;

        switch (state) {
            case Call.STATE_RINGING:
                if (!isActivityShowing())
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
        if (call == null) {
            CallDisplayState currentDisplayState = displayState.getValue();
            call = currentDisplayState == null ? null : currentDisplayState.getPrimary();
        }

        if (proximitySensor == null
                || call == null
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

        OngoingCall first = null;
        for (OngoingCall current : calls.getValue().values()) {
            if (current.getState() == state && !current.isConferenced()) {
                if (first == null || current.getSequence() < first.getSequence()) {
                    first = current;
                }
            }
        }

        return first;
    }

    @Nullable
    private OngoingCall getSecondCallWithState(int state) {
        if (calls.getValue() == null) return null;
        OngoingCall first = null;
        OngoingCall second = null;

        for (OngoingCall current : calls.getValue().values()) {
            if (current.getState() == state && !current.isConferenced()) {
                if (first == null || current.getSequence() < first.getSequence()) {
                    second = first;
                    first = current;
                } else if (second == null || current.getSequence() < second.getSequence()) {
                    second = current;
                }
            }
        }

        return second;
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
        this.callService = callService;
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

    public MutableLiveData<CallDisplayState> getDisplayState() {
        return displayState;
    }

    public MutableLiveData<Map<Call, OngoingCall>> getCalls() {
        return calls;
    }
}
