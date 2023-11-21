package dev.alenajam.opendialer.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import dev.alenajam.opendialer.App;
import dev.alenajam.opendialer.R;

import java.util.ArrayList;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class QuickResponseAdapter extends RecyclerView.Adapter<QuickResponseAdapter.ViewHolder> {

  private ArrayList<String> quickResponses;
  private Context context;
  private SharedPreferences sharedPreferences;
  private QuickResponseListener quickResponseListener;

  public QuickResponseAdapter(ArrayList<String> quickResponses, Context context, QuickResponseListener quickResponseListener, TextView placeholder) {
    this(quickResponses, context, quickResponseListener);

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

  public QuickResponseAdapter(ArrayList<String> quickResponses, Context context, QuickResponseListener quickResponseListener) {
    this.quickResponses = quickResponses;
    this.context = context;
    sharedPreferences = ((App) context.getApplicationContext()).getAppSharedPreferences();
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

    // Inflate the custom layout
    View contactView = inflater.inflate(R.layout.item_quick_response, parent, false);

    // Return a new holder instance
    ViewHolder viewHolder = new ViewHolder(contactView);
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
    holder.textView.setText(quickResponses.get(position));

    holder.layout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        quickResponseListener.clickListener(position);
      }
    });

    holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        quickResponseListener.longClickListener(position);
        return true;
      }
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

  // Provide a direct reference to each of the views within a data item
  // Used to cache the views within the item layout for fast access
  public class ViewHolder extends RecyclerView.ViewHolder {
    // Your holder should contain a member variable
    // for any view that will be set as you render a row
    public TextView textView;
    public RelativeLayout layout;

    // We also create a constructor that accepts the entire item row
    // and does the view lookups to find each subview
    public ViewHolder(View itemView) {
      // Stores the itemView in a public final member variable that can be used
      // to access the context from any ViewHolder instance.
      super(itemView);
      textView = itemView.findViewById(R.id.item_quick_response_text);
      layout = itemView.findViewById(R.id.item_quick_response_layout);
    }
  }
}