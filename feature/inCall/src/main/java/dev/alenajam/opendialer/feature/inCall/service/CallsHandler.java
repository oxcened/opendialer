package dev.alenajam.opendialer.feature.inCall.service;

import android.content.Context;
import android.telecom.Call;
import android.telecom.CallAudioState;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dev.alenajam.opendialer.feature.inCall.R;
import dev.alenajam.opendialer.feature.inCall.ui.InCallActivity;

@Singleton
public class CallsHandler {
    private final MutableLiveData<Map<Call, OngoingCall>> calls = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<CallDisplayState> displayState =
            new MutableLiveData<>(new CallDisplayState(null, null));
    private final MutableLiveData<CallAudioUiState> audioState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> canAddCall = new MutableLiveData<>();
    private InCallServiceImpl callService;
    private InCallActivity inCallActivity;

    private Context context;
    private ProximitySensor proximitySensor;
    private long nextCallSequence;
    private final CallContactResolver contactResolver;

    @Inject
    public CallsHandler(CallContactResolver contactResolver) {
        this.contactResolver = contactResolver;
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

    @MainThread
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
        resolveContact(ongoingCall);
        updateCalls();
    }

    @MainThread
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
        updateCalls();
    }

    @MainThread
    public void updateCalls() {
        Map<Call, OngoingCall> map = calls.getValue();
        if ((map.isEmpty() || getPrimaryCallToDisplay() == null) && reconcileCallsFromTelecom()) {
            map = calls.getValue();
        }

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
            // Conference creation/removal is reported through several callbacks.
            // Keep a surviving call visible while its parent relationship settles
            // instead of briefly publishing an empty screen and closing the UI.
            primary = getPreviousVisibleCall(map);
        }
        if (primary == null) {
            primary = getFirstTrackedCall(map);
        }

        if (primary != null) {
            OngoingCall secondary = getCallToDisplay(primary);
            displayState.setValue(new CallDisplayState(primary, secondary));
            handleCallNotification(primary, primary.getState());
            if (primary.getState() == Call.STATE_DIALING) attemptStartActivity();
            updateProximitySensor(primary);
        } else {
            displayState.setValue(new CallDisplayState(null, null));
        }
    }

    private boolean reconcileCallsFromTelecom() {
        if (callService == null) return false;

        Map<Call, OngoingCall> reconciledCalls = new HashMap<>(calls.getValue());
        boolean changed = false;
        for (Call call : callService.getCalls()) {
            if (call.getState() != Call.STATE_DISCONNECTED && !reconciledCalls.containsKey(call)) {
                reconciledCalls.put(
                        call,
                        new OngoingCall(context, call, this, nextCallSequence++)
                );
                resolveContact(reconciledCalls.get(call));
                changed = true;
            }
        }

        if (!changed) return false;

        // Telecom can expose a new conference parent before onCallAdded reaches
        // us, or retain it after an independent companion call is removed.
        calls.setValue(reconciledCalls);
        return true;
    }

    @MainThread
    void onCallDetailsChanged(OngoingCall ongoingCall) {
        ongoingCall.refreshIdentity();
        resolveContact(ongoingCall);
        updateCalls();
    }

    private void resolveContact(OngoingCall ongoingCall) {
        String number = ongoingCall.getCallerNumber();
        if (number.isEmpty() || ongoingCall.isConference()) return;

        contactResolver.resolve(number, contact -> {
            Map<Call, OngoingCall> currentCalls = calls.getValue();
            if (currentCalls == null || currentCalls.get(ongoingCall.getCall()) != ongoingCall) return;
            if (!number.equals(ongoingCall.getCallerNumber())) return;

            ongoingCall.applyContact(contact);
            updateCalls();
        });
    }

    @Nullable
    private OngoingCall getPreviousVisibleCall(Map<Call, OngoingCall> map) {
        CallDisplayState previous = displayState.getValue();
        if (previous == null || previous.getPrimary() == null) return null;

        OngoingCall call = previous.getPrimary();
        return map.containsValue(call) && call.getState() != Call.STATE_DISCONNECTED ? call : null;
    }

    @Nullable
    private OngoingCall getFirstTrackedCall(Map<Call, OngoingCall> map) {
        OngoingCall first = null;
        for (OngoingCall current : map.values()) {
            if (current.getState() == Call.STATE_DISCONNECTED) continue;
            if (first == null || current.getSequence() < first.getSequence()) first = current;
        }
        return first;
    }

    private void handleCallNotification(OngoingCall call, int state) {
        if (callService == null) return;
        String caller = call.getCallerName();
        if (caller == null || caller.isEmpty()) caller = call.getCallerNumber();
        if (caller == null || caller.isEmpty()) caller = context.getString(R.string.anonymous);

        switch (state) {
            case Call.STATE_RINGING:
                if (!isActivityShowing())
                    NotificationHelper.notifyIncomingCall(context, callService, caller);
                break;
            case Call.STATE_DIALING:
                NotificationHelper.notifyOutgoingCall(context, callService, caller);
                break;
            case Call.STATE_ACTIVE:
                NotificationHelper.notifyOngoingCall(context, callService, caller);
                break;
            case Call.STATE_HOLDING:
                NotificationHelper.notifyOnHoldCall(context, callService, caller);
                break;
        }
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

    @MainThread
    public void updateCallAudioState(CallAudioState newAudioState) {
        audioState.setValue(new CallAudioUiState(newAudioState.getRoute(), newAudioState.isMuted()));
        updateProximitySensor(null);
    }

    @MainThread
    public void updateCallEndpoint(int route) {
        CallAudioUiState current = audioState.getValue();
        audioState.setValue(new CallAudioUiState(route, current != null && current.isMuted()));
        updateProximitySensor(null);
    }

    @MainThread
    public void updateMuteState(boolean isMuted) {
        CallAudioUiState current = audioState.getValue();
        int route = current == null ? CallAudioState.ROUTE_EARPIECE : current.getRoute();
        audioState.setValue(new CallAudioUiState(route, isMuted));
    }

    @MainThread
    public void updateCanAddCall(boolean newCanAddCall) {
        canAddCall.setValue(newCanAddCall);
    }


    public void setup(InCallServiceImpl callService, Context context, ProximitySensor proximitySensor) {
        this.callService = callService;
        this.context = context;
        this.proximitySensor = proximitySensor;
        updateCalls();
    }

    public void tearDown() {
        callService = null;
        context = null;
        if (proximitySensor != null) proximitySensor.tearDown();
        proximitySensor = null;
    }

    public LiveData<CallAudioUiState> getAudioState() {
        return audioState;
    }

    public LiveData<Boolean> getCanAddCall() {
        return canAddCall;
    }

    public LiveData<CallDisplayState> getDisplayState() {
        return displayState;
    }

    public LiveData<Map<Call, OngoingCall>> getCalls() {
        return calls;
    }
}
