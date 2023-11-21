package dev.alenajam.opendialer.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AlertDialog;

import dev.alenajam.opendialer.R;

public class MyDialog extends AlertDialog implements View.OnClickListener {
  protected View view;
  private OnClickListener listener;
  protected Button positiveButton, negativeButton;

  public MyDialog(@NonNull Context context) {
    super(context);
    init(context);
  }

  public MyDialog(@NonNull Context context, @StyleRes int style) {
    super(context, style);
    init(context);
  }

  private void init(Context context) {
    view = LayoutInflater.from(context).inflate(R.layout.dialog_my, null);
    setView(view);
    positiveButton = view.findViewById(R.id.buttonPositive);
    negativeButton = view.findViewById(R.id.buttonNegative);
  }

  public void setTitle(String title) {
    TextView titleTextView = view.findViewById(R.id.title);
    titleTextView.setText(title);
  }

  public void setTitle(@StringRes int title) {
    TextView titleTextView = view.findViewById(R.id.title);
    titleTextView.setText(title);
  }

  public void setContent(View view) {
    FrameLayout frameLayout = this.view.findViewById(R.id.content);
    frameLayout.addView(view);
  }

  public void setPositiveButton(String label) {
    positiveButton.setVisibility(View.VISIBLE);
    positiveButton.setText(label);
    positiveButton.setOnClickListener(this);
  }

  public void setPositiveButton(@StringRes int label) {
    setPositiveButton(getContext().getString(label));
  }

  public void setNegativeButton(String label) {
    negativeButton.setVisibility(View.VISIBLE);
    negativeButton.setText(label);
    negativeButton.setOnClickListener(this);
  }

  public void setNegativeButton(@StringRes int label) {
    setNegativeButton(getContext().getString(label));
  }

  public void setOnClickListener(OnClickListener listener) {
    this.listener = listener;
  }

  @Override
  public void onClick(View v) {
    if (listener == null) return;
    if (v.getId() == R.id.buttonPositive) listener.onClick(this, BUTTON_POSITIVE);
    else if (v.getId() == R.id.buttonNegative) listener.onClick(this, BUTTON_NEGATIVE);
  }

  public interface OnClickListener {
    void onClick(MyDialog dialog, int whichButton);
  }
}
