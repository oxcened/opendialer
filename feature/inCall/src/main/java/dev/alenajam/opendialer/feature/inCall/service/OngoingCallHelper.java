package dev.alenajam.opendialer.feature.inCall.service;

import android.content.Context;
import android.telecom.Call;
import android.telecom.DisconnectCause;
import android.widget.Toast;

import androidx.annotation.Nullable;

public abstract class OngoingCallHelper {
    public static boolean handleDisconnectCause(Context context, Call call) {
        boolean hasCause = false;
        if (isDisconnectedByError(call)) {
            String cause = getDisconnectCauseDesc(call);
            hasCause = cause != null && !cause.isEmpty();
            if (hasCause) {
                Toast.makeText(context, cause, Toast.LENGTH_LONG).show();
            }
        }
        return hasCause;
    }

    @Nullable
    public static String getDisconnectCauseDesc(Call call) {
        CharSequence desc = call.getDetails().getDisconnectCause().getDescription();
        if (desc == null) return null;
        return desc.toString();
    }

    public static boolean isDisconnectedByError(Call call) {
        int code = call.getDetails().getDisconnectCause().getCode();
        return code != DisconnectCause.LOCAL && code != DisconnectCause.REMOTE;
    }
}
