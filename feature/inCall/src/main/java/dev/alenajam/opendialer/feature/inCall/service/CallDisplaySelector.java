package dev.alenajam.opendialer.feature.inCall.service;

import android.telecom.Call;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

final class CallDisplaySelector {
    private static final int[] PRIMARY_PRIORITY = {
            Call.STATE_RINGING,
            Call.STATE_DIALING,
            Call.STATE_CONNECTING,
            Call.STATE_ACTIVE,
            Call.STATE_HOLDING,
            Call.STATE_DISCONNECTING
    };
    private static final int[] SECONDARY_PRIORITY = {
            Call.STATE_ACTIVE,
            Call.STATE_HOLDING,
            Call.STATE_DISCONNECTING
    };

    private CallDisplaySelector() {
    }

    static <T> Selection<T> select(Collection<Candidate<T>> candidates) {
        List<Candidate<T>> ordered = new ArrayList<>(candidates);
        ordered.sort(Comparator.comparingLong(Candidate::getSequence));

        Candidate<T> primary = firstMatching(ordered, PRIMARY_PRIORITY, null);
        Candidate<T> secondary = firstMatching(ordered, SECONDARY_PRIORITY, primary);
        return new Selection<>(valueOf(primary), valueOf(secondary));
    }

    @Nullable
    private static <T> Candidate<T> firstMatching(
            List<Candidate<T>> candidates,
            int[] priorities,
            @Nullable Candidate<T> ignored
    ) {
        for (int state : priorities) {
            for (Candidate<T> candidate : candidates) {
                if (candidate != ignored && !candidate.isConferenced() && candidate.getState() == state) {
                    return candidate;
                }
            }
        }
        return null;
    }

    @Nullable
    private static <T> T valueOf(@Nullable Candidate<T> candidate) {
        return candidate == null ? null : candidate.getValue();
    }

    static final class Candidate<T> {
        private final T value;
        private final int state;
        private final boolean conferenced;
        private final long sequence;

        Candidate(T value, int state, boolean conferenced, long sequence) {
            this.value = value;
            this.state = state;
            this.conferenced = conferenced;
            this.sequence = sequence;
        }

        T getValue() { return value; }
        int getState() { return state; }
        boolean isConferenced() { return conferenced; }
        long getSequence() { return sequence; }
    }

    static final class Selection<T> {
        @Nullable private final T primary;
        @Nullable private final T secondary;

        Selection(@Nullable T primary, @Nullable T secondary) {
            this.primary = primary;
            this.secondary = secondary;
        }

        @Nullable T getPrimary() { return primary; }
        @Nullable T getSecondary() { return secondary; }
    }
}
