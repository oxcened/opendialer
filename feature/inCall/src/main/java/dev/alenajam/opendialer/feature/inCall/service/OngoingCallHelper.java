package dev.alenajam.opendialer.feature.inCall.service;

import android.content.Context;
import android.telecom.Call;
import android.telecom.DisconnectCause;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.List;

public abstract class OngoingCallHelper {
  public static final int CALL_TYPE_OUTGOING = 0;
  public static final int CALL_TYPE_INCOMING = 1;

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

  public static void merge(OngoingCall oCall) {
    Call call = oCall.getCall();
    if (call == null) return;

    List<Call> conferenceable = call.getConferenceableCalls();

    if (!conferenceable.isEmpty()) {
      call.conference(conferenceable.get(0));
    } else if (call.getDetails().can(Call.Details.CAPABILITY_MERGE_CONFERENCE)) {
      call.mergeConference();
    }
  }
}
