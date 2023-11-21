package dev.alenajam.opendialer.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dev.alenajam.opendialer.R;
import dev.alenajam.opendialer.util.CommonUtils;

public class EditTextDialog extends MyDialog implements View.OnClickListener {
  private Context context;
  private EditText editText;
  private TextView description;
  private LinearLayout linearLayout;
  private OnTextChangeListener onTextChangeListener = null;

  public EditTextDialog(@NonNull Context context) {
    super(context);
    this.context = context;
    linearLayout = new LinearLayout(context);
    linearLayout.setOrientation(LinearLayout.VERTICAL);
    editText = new EditText(context);
    editText.setHint(context.getString(R.string.your_text_here));
    editText.requestFocus();
    editText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (onTextChangeListener != null) {
          onTextChangeListener.onTextChange(s.toString());
        }
      }

      @Override
      public void afterTextChanged(Editable s) {

      }
    });
    setPositiveButton(context.getString(android.R.string.ok));
    setNegativeButton(context.getString(android.R.string.cancel));
    linearLayout.addView(editText);
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
  }

  public void setDescription(String string) {
    description = new TextView(context);
    description.setText(string);
    description.setTextColor(CommonUtils.getColorFromAttr(context, android.R.attr.textColorSecondary));
    description.setTextSize(13);
    description.setPadding(0, 0, 0, 20);
    linearLayout.addView(description, 0);
  }

  public void build() {
    super.setContent(linearLayout);
  }

  @Nullable
  public String getTypedString() {
    return editText.getText().toString();
  }

  public void setTypedString(String text) {
    editText.setText(text);
    editText.setSelection(text.length());
  }

  public void setOnTextChangeListener(OnTextChangeListener listener) {
    this.onTextChangeListener = listener;
  }

  public void setPositiveButtonEnabled(boolean enabled) {
    positiveButton.setEnabled(enabled);
  }

  public interface OnTextChangeListener {
    void onTextChange(String text);
  }
}
