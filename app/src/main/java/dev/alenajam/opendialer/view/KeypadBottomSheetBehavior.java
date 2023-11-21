package dev.alenajam.opendialer.view;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import dev.alenajam.opendialer.R;
import dev.alenajam.opendialer.util.CommonUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.HashSet;

public class KeypadBottomSheetBehavior extends ConstraintLayout implements
    View.OnClickListener,
    View.OnLongClickListener,
    View.OnTouchListener,
    KeyButton.KeyPressedListener {
  private static final int[] KEYBUTTON_IDS = new int[]{
      R.id.button0,
      R.id.button1,
      R.id.button2,
      R.id.button3,
      R.id.button4,
      R.id.button5,
      R.id.button6,
      R.id.button7,
      R.id.button8,
      R.id.button9,
      R.id.buttonAsterisk,
      R.id.buttonHashtag,
  };

  private static final int TONE_LENGTH_INFINITE = -1;
  /**
   * The DTMF tone volume relative to other sounds in the stream
   */
  private static final int TONE_RELATIVE_VOLUME = 80;
  /**
   * Stream type used to play the DTMF tones off call, and mapped to the volume control keys
   */
  private static final int DIAL_TONE_STREAM_TYPE = AudioManager.STREAM_DTMF;
  private final Object toneGeneratorLock = new Object();
  private final HashSet<View> pressedDialpadKeys = new HashSet<>(12);
  private Context context;
  private View rootView;
  private BottomSheetBehavior bottomSheetBehavior;
  private ImageView buttonCall;
  private ImageView buttonDelete;
  private EditText editText;
  private View divider;
  private KeypadBottomSheetBehaviorCallListener callListener;
  private KeypadTextChangeListener textChangeListener;
  private KeypadStateChangeListener stateChangeListener;
  private int state;
  private String text = "";
  private ToneGenerator toneGenerator;
  private boolean dTMFToneEnabled;

  public KeypadBottomSheetBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.context = context;
    LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    assert mInflater != null;
    rootView = mInflater.inflate(R.layout.keypad, this, true);

    setBackgroundColor(getResources().getColor(R.color.windowBackground, context.getTheme()));

    for (int id : KEYBUTTON_IDS) {
      KeyButton keyButton = findViewById(id);
      keyButton.setOnClickListener(this);
      keyButton.setKeyPressedListener(this);
    }

    findViewById(R.id.button0).setOnLongClickListener(this);

    KeyButton buttonAsterisk = findViewById(R.id.buttonAsterisk);
    KeyButton buttonHashtag = findViewById(R.id.buttonHashtag);

    buttonAsterisk.setMainTextSize(40);
    buttonAsterisk.setMainTextColor(R.color.textColorSecondary);
    buttonAsterisk.hideSubtitle();
    buttonAsterisk.setTitleTopMargin(15);

    buttonHashtag.setMainTextSize(25);
    buttonHashtag.setMainTextColor(R.color.textColorSecondary);
    buttonHashtag.hideSubtitle();

    buttonCall = findViewById(R.id.icon);
    buttonCall.setOnClickListener(this);

    buttonDelete = findViewById(R.id.buttonDelete);
    buttonDelete.setOnClickListener(this);
    buttonDelete.setOnLongClickListener(this);

    editText = findViewById(R.id.keys_text);
    editText.setShowSoftInputOnFocus(false);
    editText.setOnClickListener(this);

    editText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (!editText.hasFocus()) editText.requestFocus();
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        if (textChangeListener != null && !text.equals(s.toString()))
          textChangeListener.onKeypadTextChange(s.toString());
        text = s.toString();
      }
    });

    setOnClickListener(this);

    divider = findViewById(R.id.divider);

    //   updateUI(BottomSheetBehavior.STATE_EXPANDED);

    dTMFToneEnabled = CommonUtils.isDmtfSettingEnabled(context);
  }

  public EditText getEditText() {
    return editText;
  }

  public void setCallListener(KeypadBottomSheetBehaviorCallListener callListener) {
    this.callListener = callListener;
  }

  public void setTextChangeListener(KeypadTextChangeListener listener) {
    this.textChangeListener = listener;
  }

  public void setBottomSheetBehaviorState(int state) {
    bottomSheetBehavior.setState(state);
    this.state = state;
    if (stateChangeListener != null) stateChangeListener.onKeypadStateChange(state);
  }

  public BottomSheetBehavior getBottomSheetBehavior() {
    return bottomSheetBehavior;
  }

  public void setBottomSheetBehavior(BottomSheetBehavior bottomSheetBehavior) {
    this.bottomSheetBehavior = bottomSheetBehavior;
    bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View view, int i) {
        state = i;
        if (stateChangeListener != null) stateChangeListener.onKeypadStateChange(i);
      }

      @Override
      public void onSlide(@NonNull View view, float v) {
      }
    });
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    editText.setText(text);
  }

  public void setStateChangeListener(KeypadStateChangeListener stateChangeListener) {
    this.stateChangeListener = stateChangeListener;
  }

  public boolean isOpen() {
    return bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED;
  }

  public boolean isClosed() {
    return bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED;
  }

  public void updateUI(int state) {
    boolean isStateExpanded = state == BottomSheetBehavior.STATE_EXPANDED, isStateCollapsed = state == BottomSheetBehavior.STATE_COLLAPSED;

    editText.setFocusableInTouchMode(isStateExpanded);

    if (isStateExpanded) {
      editText.requestFocus();
      editText.setSelection(editText.getText().length());
    } else editText.clearFocus();

    buttonDelete.setVisibility(isStateCollapsed ? INVISIBLE : VISIBLE);

    divider.setVisibility(isStateCollapsed ? INVISIBLE : VISIBLE);

    if (isStateExpanded) {
      buttonDelete.setAlpha(1f);
      divider.setAlpha(1f);
    }
  }

  @Override
  public void onClick(View v) {
    String selectedText = CommonUtils.getEditTextSelectedText(editText);
    int selectionStart = editText.getSelectionStart();

    if (v.getId() == getId() || v.getId() == R.id.keys_text) {
      setBottomSheetBehaviorState(BottomSheetBehavior.STATE_EXPANDED);
      return;
    } else if (v.getId() == R.id.icon) call();
    else if (v.getId() == R.id.buttonDelete) delete(selectedText, selectionStart);
    else {
      KeyButton keyButton = ((KeyButton) v);
      write(keyButton.getMainText(), selectedText, selectionStart);
//            new Thread(() -> {
//                ToneGenerator toneGenerator = CommonUtils.getToneGenerator();
//                CommonUtils.playDtmfSound(context, toneGenerator, keyButton.getMainText());
//                new Handler(Looper.getMainLooper()).postDelayed(toneGenerator::release, 50);
//            }).start();
    }
    //performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
  }

  @Override
  public boolean onLongClick(View v) {
    String selectedText = CommonUtils.getEditTextSelectedText(editText);
    int selectionStart = editText.getSelectionStart();

    if (v.getId() == R.id.buttonDelete) editText.setText(null);
    else if (v.getId() == R.id.button0)
      write(((KeyButton) v).getSecondaryText(), selectedText, selectionStart);
    return true;
  }

  @Override
  public boolean onTouch(View v, MotionEvent event) {
    return false;
  }

  @Override
  public void onKeyPressed(View view, boolean pressed) {
    if (pressed) {
      switch (view.getId()) {
        case R.id.button1:
          onKeyPressedDown(Key.KEY_1);
          break;
        case R.id.button2:
          onKeyPressedDown(Key.KEY_2);
          break;
        case R.id.button3:
          onKeyPressedDown(Key.KEY_3);
          break;
        case R.id.button4:
          onKeyPressedDown(Key.KEY_4);
          break;
        case R.id.button5:
          onKeyPressedDown(Key.KEY_5);
          break;
        case R.id.button6:
          onKeyPressedDown(Key.KEY_6);
          break;
        case R.id.button7:
          onKeyPressedDown(Key.KEY_7);
          break;
        case R.id.button8:
          onKeyPressedDown(Key.KEY_8);
          break;
        case R.id.button9:
          onKeyPressedDown(Key.KEY_9);
          break;
        case R.id.button0:
          onKeyPressedDown(Key.KEY_0);
          break;
        case R.id.buttonHashtag:
          onKeyPressedDown(Key.KEY_POUND);
          break;
        case R.id.buttonAsterisk:
          onKeyPressedDown(Key.KEY_STAR);
          break;
      }
      pressedDialpadKeys.add(view);
    } else {
      pressedDialpadKeys.remove(view);
      if (pressedDialpadKeys.isEmpty()) {
        stopTone();
      }
    }
  }

  private void onKeyPressedDown(Key key) {
    switch (key) {
      case KEY_1:
        playTone(ToneGenerator.TONE_DTMF_1, TONE_LENGTH_INFINITE);
        break;
      case KEY_2:
        playTone(ToneGenerator.TONE_DTMF_2, TONE_LENGTH_INFINITE);
        break;
      case KEY_3:
        playTone(ToneGenerator.TONE_DTMF_3, TONE_LENGTH_INFINITE);
        break;
      case KEY_4:
        playTone(ToneGenerator.TONE_DTMF_4, TONE_LENGTH_INFINITE);
        break;
      case KEY_5:
        playTone(ToneGenerator.TONE_DTMF_5, TONE_LENGTH_INFINITE);
        break;
      case KEY_6:
        playTone(ToneGenerator.TONE_DTMF_6, TONE_LENGTH_INFINITE);
        break;
      case KEY_7:
        playTone(ToneGenerator.TONE_DTMF_7, TONE_LENGTH_INFINITE);
        break;
      case KEY_8:
        playTone(ToneGenerator.TONE_DTMF_8, TONE_LENGTH_INFINITE);
        break;
      case KEY_9:
        playTone(ToneGenerator.TONE_DTMF_9, TONE_LENGTH_INFINITE);
        break;
      case KEY_0:
        playTone(ToneGenerator.TONE_DTMF_0, TONE_LENGTH_INFINITE);
        break;
      case KEY_POUND:
        playTone(ToneGenerator.TONE_DTMF_P, TONE_LENGTH_INFINITE);
        break;
      case KEY_STAR:
        playTone(ToneGenerator.TONE_DTMF_S, TONE_LENGTH_INFINITE);
        break;
    }

    performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
  }

  private void playTone(int tone, int durationMs) {
    // if local tone playback is disabled, just return.
    if (!dTMFToneEnabled) {
      return;
    }

    // Also do nothing if the phone is in silent mode.
    // We need to re-check the ringer mode for *every* playTone()
    // call, rather than keeping a local flag that's updated in
    // onResume(), since it's possible to toggle silent mode without
    // leaving the current activity (via the ENDCALL-longpress menu.)
    if (CommonUtils.isRingerModeSilentOrVibrate(context)) {
      return;
    }

    synchronized (toneGeneratorLock) {
      if (toneGenerator == null) {
        Log.w(KeypadBottomSheetBehavior.class.getSimpleName(), "mToneGenerator == null, tone: " + tone);
        return;
      }

      // Start the new tone (will stop any playing tone)
      toneGenerator.startTone(tone, durationMs);
    }
  }

  /**
   * Stop the tone if it is played.
   */
  private void stopTone() {
    // if local tone playback is disabled, just return.
    if (!dTMFToneEnabled) {
      return;
    }
    synchronized (toneGeneratorLock) {
      if (toneGenerator == null) {
        Log.w(KeypadBottomSheetBehavior.class.getSimpleName(), "mToneGenerator == null");
        return;
      }
      toneGenerator.stopTone();
    }
  }

  private void call() {
    String number = editText.getText().toString();
    if (callListener != null) callListener.onCall(number);
  }

  private void delete(String selectedText, int selectionStart) {
    Editable oldText = editText.getText();
    if (oldText.toString().isEmpty()) return;

    if (selectedText == null) {
      if (selectionStart > 0) {
        editText.setText(oldText.delete(selectionStart - 1, selectionStart).toString());
        editText.setSelection(selectionStart - 1);
      }
    } else {
      editText.setText(oldText.delete(selectionStart, editText.getSelectionEnd()));
      editText.setSelection(selectionStart);
    }
  }

  private void write(String newText, String selectedText, int selectionStart) {
    Editable oldText = editText.getText();

    if (selectedText == null) {
      editText.setText(oldText.insert(selectionStart, newText));
    } else {
      editText.setText(oldText.delete(selectionStart, editText.getSelectionEnd()).insert(selectionStart, newText));
    }

    editText.setSelection(selectionStart + 1);
  }

  public void open() {
    setBottomSheetBehaviorState(BottomSheetBehavior.STATE_EXPANDED);
  }

  public void close() {
    setBottomSheetBehaviorState(BottomSheetBehavior.STATE_COLLAPSED);
  }

  public void init() {
    synchronized (toneGeneratorLock) {
      if (toneGenerator == null) {
        try {
          toneGenerator = new ToneGenerator(DIAL_TONE_STREAM_TYPE, TONE_RELATIVE_VOLUME);
        } catch (RuntimeException e) {
          Log.e(
              KeypadBottomSheetBehavior.class.getSimpleName(),
              "Exception caught while creating local tone generator: " + e);
          toneGenerator = null;
        }
      }
    }
  }

  public void tearDown() {
    synchronized (toneGeneratorLock) {
      if (toneGenerator != null) {
        toneGenerator.release();
        toneGenerator = null;
      }
    }
  }

  public enum Key {
    KEY_1,
    KEY_2,
    KEY_3,
    KEY_4,
    KEY_5,
    KEY_6,
    KEY_7,
    KEY_8,
    KEY_9,
    KEY_0,
    KEY_POUND,
    KEY_STAR,
    KEY_DEL
  }

  public interface KeypadBottomSheetBehaviorCallListener {
    void onCall(String number);
  }

  public interface KeypadTextChangeListener {
    void onKeypadTextChange(String text);
  }

  public interface KeypadStateChangeListener {
    void onKeypadStateChange(int state);
  }
}
