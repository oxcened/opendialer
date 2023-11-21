package dev.alenajam.opendialer.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import dev.alenajam.opendialer.R;
import dev.alenajam.opendialer.util.CommonUtilsKt;

public class SearchView extends LinearLayout {
  private Context context;
  private EditTextFocusChangeListener focusListener;
  private EditTextChangeListener textListener;
  private SearchViewOpenListener openListener;
  private ImageView icon;
  private boolean hasFocus = false;
  private boolean isOpen = false;
  private EditText editText;

  public SearchView(Context context) {
    super(context);
    init(context);
  }

  public SearchView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(Context context) {
    this.context = context;
    LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    assert mInflater != null;
    View rootView = mInflater.inflate(R.layout.search_view, this, true);

    editText = findViewById(R.id.searchEditText);
    icon = findViewById(R.id.searchIcon);

    editText.setOnFocusChangeListener((v, hasFocus) -> {
      if (focusListener != null) focusListener.onFocusChange(editText, hasFocus);
      setOpen(hasFocus);
    });

    editText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (textListener != null) {
          textListener.onTextChange(editText, s.toString());
        }
      }

      @Override
      public void afterTextChanged(Editable s) {

      }
    });

    icon.setOnClickListener(v -> {
      setOpen(!this.isOpen);
    });

    RelativeLayout layout = findViewById(R.id.layout);
    layout.setOnClickListener(v -> {
      setOpen(true);
    });
  }

  public boolean isFocused() {
    return hasFocus;
  }

  public void setFocus(boolean focus) {
    if (focus) {
      editText.requestFocus();
    } else {
      editText.clearFocus();
    }
  }

  public void onOpenChange(boolean isOpen) {
    if (openListener != null) {
      openListener.onOpen(isOpen);
    }

    if (isOpen) {
      icon.setImageDrawable(context.getDrawable(R.drawable.ic_arrow_back));
      setFocus(true);
    } else {
      icon.setImageDrawable(context.getDrawable(R.drawable.icon_12));
      setFocus(false);
      clear();
    }
  }

  private void updateKeyboard() {
    if (isOpen) {
      CommonUtilsKt.showInputMethod(editText);
    } else {
      closeKeyboard();
    }
  }

  public void closeKeyboard() {
    CommonUtilsKt.hideInputMethod(editText);
  }

  public void open() {
    setOpen(true);
  }

  public void close() {
    setOpen(false);
  }

  public boolean isOpen() {
    return isOpen;
  }

  private void setOpen(boolean isOpen) {
    if (this.isOpen != isOpen) {
      this.isOpen = isOpen;
      onOpenChange(this.isOpen);
    }

    updateKeyboard();
  }

  public void clear() {
    editText.setText(null);
  }

  public String getText() {
    return editText.getText().toString();
  }

  public void setText(String text) {
    editText.setText(text);
  }

  public void setFocusChangeListener(EditTextFocusChangeListener listener) {
    this.focusListener = listener;
  }

  public void setOpenListener(SearchViewOpenListener openListener) {
    this.openListener = openListener;
  }

  public void setTextListener(EditTextChangeListener textListener) {
    this.textListener = textListener;
  }

  public interface EditTextFocusChangeListener {
    void onFocusChange(EditText editText, boolean hasFocus);
  }

  public interface EditTextChangeListener {
    void onTextChange(EditText editText, String text);
  }

  public interface SearchViewOpenListener {
    void onOpen(boolean isOpen);
  }
}
