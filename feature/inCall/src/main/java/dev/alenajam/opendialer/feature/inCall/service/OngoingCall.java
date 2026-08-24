package dev.alenajam.opendialer.feature.inCall.service;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.telecom.Call;
import android.telecom.VideoProfile;

import java.util.List;

import javax.annotation.Nullable;

import dev.alenajam.opendialer.core.common.CommonUtils;
import dev.alenajam.opendialer.core.common.Contact;
import dev.alenajam.opendialer.feature.inCall.R;

public class OngoingCall {
    private static final int DTMF_DURATION_MS = 300;
    private final Call call;
    private String callerNumber = "", callerNumberLabel = "", callerName, callerImageUri = null;
    private long startTime = -1, totalTime = 0;
    private final Context context;
    private final CallsHandler callsHandler;
    private final long sequence;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable stopDtmfTone;

    private final Call.Callback callback = new Call.Callback() {
        @Override
        public void onStateChanged(Call call, int newState) {
            super.onStateChanged(call, newState);
            updateState(newState);
            if (newState != Call.STATE_DISCONNECTED) callsHandler.updateCalls();
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
            callsHandler.onCallDetailsChanged(OngoingCall.this);
        }
    };

    public OngoingCall(Context context, Call call, CallsHandler callsHandler, long sequence) {
        this.call = call;
        this.context = context;
        this.callsHandler = callsHandler;
        this.sequence = sequence;
        this.stopDtmfTone = call::stopDtmfTone;

        call.registerCallback(callback);
        refreshIdentity();
        updateState(getState());
    }

    public void refreshIdentity() {
        callerName = null;
        callerNumber = "";
        callerNumberLabel = "";
        callerImageUri = null;
        if (isConference()) {
            callerName = context.getString(R.string.conference_call);
        } else if (!isAnonymous()) {
            Uri numberUri = call.getDetails().getHandle();
            callerNumber = numberUri.getSchemeSpecificPart();
            callerName = callerNumber;
        }
    }

    public void applyContact(@Nullable Contact contact) {
        if (contact == null) return;
        callerName = contact.getName();
        callerImageUri = contact.getImageUri();
        callerNumberLabel = ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                context.getResources(),
                contact.getPhoneType(),
                contact.getPhoneLabel()
        ).toString();
    }

    public void tearDown() {
        mainHandler.removeCallbacks(stopDtmfTone);
        call.stopDtmfTone();
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
                callsHandler.removeCall(call);
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

    public String getCallerNumber() {
        return callerNumber;
    }

    public String getCallerNumberLabel() {
        return callerNumberLabel;
    }

    @Nullable
    public String getCallerName() {
        return callerName;
    }

    @Nullable
    public String getCallerImageUri() {
        return callerImageUri;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public long getSequence() {
        return sequence;
    }

    public Integer getState() {
        if (call == null) return null;
        return call.getState();
    }

    public void answer() {
        if (call == null || getState() != Call.STATE_RINGING) return;
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
        } else if (canBeHeld()) {
            call.hold();
        }
    }

    public void hold(boolean hold) {
        if (call == null) return;
        if (hold && canBeHeld()) call.hold();
        else if (!hold && getState() == Call.STATE_HOLDING) call.unhold();
    }

    public void playDtmf(char digit) {
        if (call == null || getState() != Call.STATE_ACTIVE) return;
        mainHandler.removeCallbacks(stopDtmfTone);
        call.stopDtmfTone();
        call.playDtmfTone(digit);
        mainHandler.postDelayed(stopDtmfTone, DTMF_DURATION_MS);
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

    public boolean canBeHeld() {
        if (call == null) return false;
        return getState() == Call.STATE_HOLDING
                || call.getDetails().can(Call.Details.CAPABILITY_HOLD);
    }

    public boolean canBeSplit() {
        if (call == null) return false;
        return call.getDetails().can(Call.Details.CAPABILITY_SEPARATE_FROM_CONFERENCE);
    }

    public void split() {
        if (call == null || !canBeSplit()) return;
        call.splitFromConference();
    }

    public void merge() {
        if (call == null || !canBeMerged()) return;

        List<Call> conferenceableCalls = call.getConferenceableCalls();
        if (!conferenceableCalls.isEmpty()) {
            call.conference(conferenceableCalls.get(0));
        } else if (call.getDetails().can(Call.Details.CAPABILITY_MERGE_CONFERENCE)) {
            call.mergeConference();
        }
    }

    public boolean isConference() {
        if (call == null) return false;

        return call.getDetails().hasProperty(Call.Details.PROPERTY_CONFERENCE);
    }

    public boolean isConferenced() {
        return call != null && call.getParent() != null;
    }
}
