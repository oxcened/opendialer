package dev.alenajam.opendialer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import dev.alenajam.opendialer.R;
import dev.alenajam.opendialer.model.CallOption;

import java.util.ArrayList;

public class CallOptionsAdapter extends RecyclerView.Adapter<CallOptionsAdapter.ViewHolder> {

  private ArrayList<CallOption> options;
  private Context context;
  private CallOptionsAdapterListener listener;

  public CallOptionsAdapter(ArrayList<CallOption> options, Context context, CallOptionsAdapterListener listener) {
    this.options = options;
    this.context = context;
    this.listener = listener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    Context context = parent.getContext();
    LayoutInflater inflater = LayoutInflater.from(context);

    // Inflate the custom layout
    View contactView = inflater.inflate(R.layout.item_call_option, parent, false);

    // Return a new holder instance
    ViewHolder viewHolder = new ViewHolder(contactView);
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
    CallOption currentOption = options.get(position);

    holder.title.setText(currentOption.getText());
    holder.icon.setImageDrawable(context.getDrawable(currentOption.getIcon()));
    holder.layout.setOnClickListener(v -> listener.onClick(currentOption.getId()));
  }

  @Override
  public int getItemCount() {
    return options.size();
  }

  public void addItem(CallOption item) {
    this.options.add(item);
    notifyItemInserted(this.options.size() - 1);
  }

  public int getItemIndex(CallOption option) {
    return this.options.indexOf(option);
  }

  @Nullable
  public CallOption getItem(int id) {
    for (int i = 0; i < this.options.size(); i++) {
      if (this.options.get(i).getId() == id) {
        return this.options.get(i);
      }
    }
    return null;
  }

  public interface CallOptionsAdapterListener {
    void onClick(int whichOption);
  }

  // Provide a direct reference to each of the views within a data item
  // Used to cache the views within the item layout for fast access
  protected class ViewHolder extends RecyclerView.ViewHolder {
    // Your holder should contain a member variable
    // for any view that will be set as you render a row
    private TextView title;
    private ImageView icon;
    private LinearLayout layout;

    // We also create a constructor that accepts the entire item row
    // and does the view lookups to find each subview
    private ViewHolder(View itemView) {
      // Stores the itemView in a public final member variable that can be used
      // to access the context from any ViewHolder instance.
      super(itemView);
      title = itemView.findViewById(R.id.title);
      icon = itemView.findViewById(R.id.icon);
      layout = itemView.findViewById(R.id.layout);
    }
  }
}