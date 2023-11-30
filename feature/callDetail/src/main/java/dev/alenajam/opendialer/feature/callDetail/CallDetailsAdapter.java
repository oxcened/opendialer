package dev.alenajam.opendialer.feature.callDetail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import dev.alenajam.opendialer.core.common.CommonUtils;
import dev.alenajam.opendialer.data.calls.DetailCall;

public class CallDetailsAdapter extends RecyclerView.Adapter<CallDetailsAdapter.ViewHolder> {
  private DateFormat callDateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.getDefault());

  private List<DetailCall> calls;
  private Context context;

  public CallDetailsAdapter(List<DetailCall> calls, Context context) {
    this.calls = calls;
    this.context = context;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    Context context = parent.getContext();
    LayoutInflater inflater = LayoutInflater.from(context);

    // Inflate the custom layout
    View contactView = inflater.inflate(R.layout.item_call_details, parent, false);

    // Return a new holder instance
    ViewHolder viewHolder = new ViewHolder(contactView);
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
    DetailCall currentCall = calls.get(position);

    holder.subtitle.setText(callDateFormat.format(currentCall.getDate()));
    if (currentCall.getDuration() == 0) {
      holder.duration.setVisibility(View.GONE);
    } else {
      holder.duration.setText(CommonUtils.getDurationTimeStringMinimal(currentCall.getDuration() * 1000));
    }

    // TODO refactor
    int drawableRes = -1, text = -1;
    switch (currentCall.getType()) {
      case OUTGOING:
        drawableRes = R.drawable.icon_16;
        text = R.string.outgoing_call;
        break;
      case INCOMING:
      case ANSWERED_EXTERNALLY:
        drawableRes = R.drawable.icon_21;
        text = R.string.incoming_call;
        break;
      case MISSED:
        drawableRes = R.drawable.icon_22;
        text = R.string.missed_call;
        break;
      case REJECTED:
        drawableRes = R.drawable.icon_22;
        text = R.string.rejected_call;
        break;
      case BLOCKED:
        drawableRes = R.drawable.icon_18;
        text = R.string.blocked_call;
        break;
      case VOICEMAIL:
        drawableRes = R.drawable.icon_09;
        text = R.string.voicemail_call;
        break;
    }

    if (drawableRes != -1) {
      holder.title.setText(context.getString(text));
      holder.icon.setImageDrawable(context.getDrawable(drawableRes));
    }
  }

  @Override
  public int getItemCount() {
    return calls.size();
  }

  // Provide a direct reference to each of the views within a data item
  // Used to cache the views within the item layout for fast access
  protected class ViewHolder extends RecyclerView.ViewHolder {
    // Your holder should contain a member variable
    // for any view that will be set as you render a row
    private TextView title, subtitle, duration;
    private ImageView icon;

    // We also create a constructor that accepts the entire item row
    // and does the view lookups to find each subview
    private ViewHolder(View itemView) {
      // Stores the itemView in a public final member variable that can be used
      // to access the context from any ViewHolder instance.
      super(itemView);
      title = itemView.findViewById(R.id.title);
      subtitle = itemView.findViewById(R.id.subtitle);
      duration = itemView.findViewById(R.id.duration);
      icon = itemView.findViewById(R.id.icon);
    }
  }
}