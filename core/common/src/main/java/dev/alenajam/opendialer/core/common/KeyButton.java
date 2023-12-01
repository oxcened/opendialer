package dev.alenajam.opendialer.core.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

public class KeyButton extends ConstraintLayout {
  private Context context;
  private View rootView;
  private String mainText, secondaryText;
  private TextView textView1, textView2;
  private KeyPressedListener keyPressedListener;

  public KeyButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.context = context;
    LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    assert mInflater != null;
    rootView = mInflater.inflate(R.layout.key_button, this, true);

    TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.KeyButton);

    int key = typedArray.getInt(0, 0);

    typedArray.recycle();

    textView1 = rootView.findViewById(R.id.textView1);
    textView2 = rootView.findViewById(R.id.textView2);

    mainText = String.valueOf(key);
    secondaryText = "";

    switch (key) {
      case 0:
        secondaryText = "+";
      case 1:
        break;
      case 2:
        secondaryText = "abc";
        break;
      case 3:
        secondaryText = "def";
        break;
      case 4:
        secondaryText = "ghi";
        break;
      case 5:
        secondaryText = "jkl";
        break;
      case 6:
        secondaryText = "mno";
        break;
      case 7:
        secondaryText = "pqrs";
        break;
      case 8:
        secondaryText = "tuv";
        break;
      case 9:
        secondaryText = "wxyz";
        break;
      case 10:
        mainText = "*";
        break;
      case 11:
        mainText = "#";
        break;
    }

    textView1.setText(mainText);
    textView2.setText(secondaryText);

    setSoundEffectsEnabled(false);
  }

  public String getMainText() {
    return mainText;
  }

  public String getSecondaryText() {
    return secondaryText;
  }

  public void setMainTextSize(float sp) {
    textView1.setTextSize(sp);
  }

  public void setMainTextColor(@ColorRes int color) {
    textView1.setTextColor(ContextCompat.getColor(context, color));
  }

  public void hideSubtitle() {
    textView2.setVisibility(View.GONE);
  }

  public void setTitleTopMargin(float margin) {
    LayoutParams layoutParams = (LayoutParams) textView1.getLayoutParams();
    layoutParams.topMargin = (int) CommonUtils.convertDpToPixels(margin, context);
    textView1.setLayoutParams(layoutParams);
  }

  @Override
  public void setPressed(boolean pressed) {
    super.setPressed(pressed);
    if (keyPressedListener != null) {
      keyPressedListener.onKeyPressed(this, pressed);
    }
  }

  public void setKeyPressedListener(KeyPressedListener keyPressedListener) {
    this.keyPressedListener = keyPressedListener;
  }

  interface KeyPressedListener {
    void onKeyPressed(View view, boolean pressed);
  }
}
