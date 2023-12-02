package dev.alenajam.opendialer.feature.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class QuickResponseAdapter extends RecyclerView.Adapter<QuickResponseAdapter.ViewHolder> {
  private ArrayList<String> quickResponses;
  private QuickResponseListener quickResponseListener;

  public QuickResponseAdapter(ArrayList<String> quickResponses, QuickResponseListener quickResponseListener, TextView placeholder) {
    this(quickResponses, quickResponseListener);

    registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
      @Override
      public void onChanged() {
        updatePlaceholder(placeholder);
      }

      @Override
      public void onItemRangeInserted(int positionStart, int itemCount) {
        updatePlaceholder(placeholder);
      }

      @Override
      public void onItemRangeRemoved(int positionStart, int itemCount) {
        updatePlaceholder(placeholder);
      }
    });

    updatePlaceholder(placeholder);
  }

  public QuickResponseAdapter(ArrayList<String> quickResponses, QuickResponseListener quickResponseListener) {
    this.quickResponses = quickResponses;
    this.quickResponseListener = quickResponseListener;
  }

  private void updatePlaceholder(TextView placeholder) {
    if (quickResponses.isEmpty()) {
      if (placeholder.getVisibility() != View.VISIBLE)
        placeholder.setVisibility(View.VISIBLE);
    } else {
      if (placeholder.getVisibility() != View.GONE)
        placeholder.setVisibility(View.GONE);
    }
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    Context context = parent.getContext();
    LayoutInflater inflater = LayoutInflater.from(context);
    View contactView = inflater.inflate(R.layout.item_quick_response, parent, false);
    return new ViewHolder(contactView);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
    holder.textView.setText(quickResponses.get(position));

    holder.layout.setOnClickListener(v -> quickResponseListener.clickListener(position));

    holder.layout.setOnLongClickListener(v -> {
      quickResponseListener.longClickListener(position);
      return true;
    });
  }

  @Override
  public int getItemCount() {
    return quickResponses.size();
  }

  public interface QuickResponseListener {
    void clickListener(int position);

    void longClickListener(int position);
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    public TextView textView;
    public RelativeLayout layout;

    public ViewHolder(View itemView) {
      super(itemView);
      textView = itemView.findViewById(R.id.item_quick_response_text);
      layout = itemView.findViewById(R.id.item_quick_response_layout);
    }
  }
}