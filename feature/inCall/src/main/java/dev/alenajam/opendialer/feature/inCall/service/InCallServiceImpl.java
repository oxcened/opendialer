package dev.alenajam.opendialer.feature.inCall.service;

import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.telecom.Call;
import android.telecom.CallAudioState;
import android.telecom.CallEndpoint;
import android.telecom.InCallService;

import androidx.annotation.RequiresApi;

import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;
import java.util.List;

@AndroidEntryPoint
public class InCallServiceImpl extends InCallService {
    @Inject
    CallsHandler callHandler;
    @Inject
    TelecomAdapter telecomAdapter;

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
        telecomAdapter.onLegacyAudioStateChanged(audioState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            callHandler.updateCallAudioState(audioState);
        }
    }

    @Override
    @RequiresApi(34)
    public void onCallEndpointChanged(CallEndpoint callEndpoint) {
        super.onCallEndpointChanged(callEndpoint);
        telecomAdapter.onCallEndpointChanged(callEndpoint);
        callHandler.updateCallEndpoint(TelecomAdapter.toLegacyRoute(callEndpoint.getEndpointType()));
    }

    @Override
    @RequiresApi(34)
    public void onAvailableCallEndpointsChanged(List<CallEndpoint> availableEndpoints) {
        super.onAvailableCallEndpointsChanged(availableEndpoints);
        telecomAdapter.onAvailableCallEndpointsChanged(availableEndpoints);
    }

    @Override
    @RequiresApi(34)
    public void onMuteStateChanged(boolean isMuted) {
        super.onMuteStateChanged(isMuted);
        telecomAdapter.onMuteStateChanged(isMuted);
        callHandler.updateMuteState(isMuted);
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
        telecomAdapter.attach(this);

        return super.onBind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        callHandler.tearDown();
        telecomAdapter.detach(this);

        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
