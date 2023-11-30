package dev.alenajam.opendialer.feature.contacts;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.alenajam.opendialer.core.common.CircleTransform;
import dev.alenajam.opendialer.core.common.CommonUtils;
import dev.alenajam.opendialer.data.contacts.DialerContact;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
  private final List<Integer> colorList = Arrays.asList(
      Color.parseColor("#4FAF44"),
      Color.parseColor("#F6D145"),
      Color.parseColor("#FF9526"),
      Color.parseColor("#EF4423"),
      Color.parseColor("#328AF0")
  );

  private ColorGenerator generator = ColorGenerator.create(colorList);
  private Transformation circleTransform = new CircleTransform();
  private Activity context;
  private ArrayList<DialerContact> contacts = new ArrayList<>();

  public ContactAdapter(Activity context) {
    this.context = context;
  }

  public ArrayList<DialerContact> getContacts() {
    return this.contacts;
  }

  public void setContacts(ArrayList<DialerContact> contacts) {
    this.contacts.clear();
    this.contacts.addAll(contacts);
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    String name = contacts.get(position).getName();

    holder.textView.setText(name);

    holder.layout.setOnClickListener(v -> {
      CommonUtils.hideKeyboard(context);
      CommonUtils.showContactDetail(context, contacts.get(position).getId());
    });

    String photoUri = contacts.get(position).getImage();
    if (photoUri != null && photoUri.isEmpty()) photoUri = null;

    Picasso.get()
        .load(photoUri)
        .placeholder(
            CommonUtilsKt.getContactImagePlaceholder(context, contacts.get(position), generator)
        )
        .transform(circleTransform)
        .into(holder.contactIcon);

    holder.starredIcon.setVisibility(contacts.get(position).getStarred() ? View.VISIBLE : View.GONE);
  }

  @Override
  public int getItemCount() {
    return contacts.size();
  }

  protected class ViewHolder extends RecyclerView.ViewHolder {
    private ConstraintLayout layout;
    private TextView textView;
    private ImageView contactIcon, starredIcon;

    private ViewHolder(@NonNull View itemView) {
      super(itemView);
      layout = itemView.findViewById(R.id.layout);
      textView = itemView.findViewById(R.id.text);
      contactIcon = itemView.findViewById(R.id.icon_contact);
      starredIcon = itemView.findViewById(R.id.icon_starred);
    }
  }
}