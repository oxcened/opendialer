package dev.alenajam.opendialer.feature.inCall.ui;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import dev.alenajam.opendialer.core.common.KeyButton;
import dev.alenajam.opendialer.core.common.MyBottomSheetDialog;
import dev.alenajam.opendialer.feature.inCall.R;

public class DtmfKeypadBottomDialog extends MyBottomSheetDialog implements View.OnClickListener {
  private KeyButton button0, button1, button2, button3, button4, button5, button6, button7, button8, button9, buttonAsterisk, buttonHashtag;
  private TextView keysText;
  private OnKeyClickListener listener;

  public DtmfKeypadBottomDialog(Context context) {
    super(context, R.layout.dialog_keypad);
    button0 = findViewById(R.id.button0);
    button0.setOnClickListener(this);
    button1 = findViewById(R.id.button1);
    button1.setOnClickListener(this);
    button2 = findViewById(R.id.button2);
    button2.setOnClickListener(this);
    button3 = findViewById(R.id.button3);
    button3.setOnClickListener(this);
    button4 = findViewById(R.id.button4);
    button4.setOnClickListener(this);
    button5 = findViewById(R.id.button5);
    button5.setOnClickListener(this);
    button6 = findViewById(R.id.button6);
    button6.setOnClickListener(this);
    button7 = findViewById(R.id.button7);
    button7.setOnClickListener(this);
    button8 = findViewById(R.id.button8);
    button8.setOnClickListener(this);
    button9 = findViewById(R.id.button9);
    button9.setOnClickListener(this);
    buttonAsterisk = findViewById(R.id.buttonAsterisk);
    buttonAsterisk.setOnClickListener(this);
    buttonHashtag = findViewById(R.id.buttonHashtag);
    buttonHashtag.setOnClickListener(this);
    keysText = findViewById(R.id.keys_text);
  }

  public OnKeyClickListener getListener() {
    return listener;
  }

  public void setListener(OnKeyClickListener listener) {
    this.listener = listener;
  }

  public String getKeysText() {
    return this.keysText.getText().toString();
  }

  public void setKeysText(String text) {
    this.keysText.setText(text);
  }

  @Override
  public void onClick(View v) {
    keysText.append(((KeyButton) v).getMainText());
    if (listener != null) {
      int id = v.getId();

      if (id == R.id.button0) {
        listener.onKeypadChoice(Key.ZERO);
      } else if (id == R.id.button1) {
        listener.onKeypadChoice(Key.ONE);
      } else if (id == R.id.button2) {
        listener.onKeypadChoice(Key.TWO);
      } else if (id == R.id.button3) {
        listener.onKeypadChoice(Key.THREE);
      } else if (id == R.id.button4) {
        listener.onKeypadChoice(Key.FOUR);
      } else if (id == R.id.button5) {
        listener.onKeypadChoice(Key.FIVE);
      } else if (id == R.id.button6) {
        listener.onKeypadChoice(Key.SIX);
      } else if (id == R.id.button7) {
        listener.onKeypadChoice(Key.SEVEN);
      } else if (id == R.id.button8) {
        listener.onKeypadChoice(Key.EIGHT);
      } else if (id == R.id.button9) {
        listener.onKeypadChoice(Key.NINE);
      } else if (id == R.id.buttonAsterisk) {
        listener.onKeypadChoice(Key.ASTERISK);
      } else if (id == R.id.buttonHashtag) {
        listener.onKeypadChoice(Key.HASHTAG);
      }
    }
  }

  public enum Key {
    ZERO('0'),
    ONE('1'),
    TWO('2'),
    THREE('3'),
    FOUR('4'),
    FIVE('5'),
    SIX('6'),
    SEVEN('7'),
    EIGHT('8'),
    NINE('9'),
    ASTERISK('*'),
    HASHTAG('#');

    private final char value;

    Key(char value) {
      this.value = value;
    }

    public char getValue() {
      return value;
    }
  }

  public interface OnKeyClickListener {
    void onKeypadChoice(Key whichKey);
  }
}
