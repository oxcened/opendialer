package dev.alenajam.opendialer.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import dev.alenajam.opendialer.App;
import dev.alenajam.opendialer.R;
import dev.alenajam.opendialer.adapter.QuickResponseAdapter;
import dev.alenajam.opendialer.helper.SharedPreferenceHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class RefuseWithMessageDialog extends MyBottomSheetDialog implements QuickResponseAdapter.QuickResponseListener, View.OnClickListener {
  private ArrayList<String> quickResponseList;
  private Context context;
  private Button cancelButton;
  private RefuseWithMessageDialogChoiceListener dialogListener;
  private SharedPreferences sharedPreferences;

  public RefuseWithMessageDialog(Context context, RefuseWithMessageDialogChoiceListener listener) {
    super(context, R.layout.dialog_refuse_with_message);

    this.context = context;

    dialogListener = listener;

    this.sharedPreferences = ((App) context.getApplicationContext()).getAppSharedPreferences();

    cancelButton = findViewById(R.id.text_cancel);
    cancelButton.setOnClickListener(this);

    RecyclerView recyclerView = findViewById(R.id.recycler_view_quick_responses);
    recyclerView.setLayoutManager(new LinearLayoutManager(context));

    String json = sharedPreferences.getString(SharedPreferenceHelper.SP_QUICK_RESPONSES, "");
    quickResponseList = new Gson().fromJson(json, new TypeToken<List<String>>() {
    }.getType());
    quickResponseList.add(context.getString(R.string.write_your_own));

    recyclerView.setAdapter(new QuickResponseAdapter(quickResponseList, this));
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.text_cancel) {
      hide();
    }
  }

  @Override
  public void clickListener(int position) {
    if (position == quickResponseList.size() - 1) {
      //write your own
      writeYourOwnDialog();
    } else {
      hide();
      if (dialogListener != null)
        dialogListener.onRefuseWithMessageChoice(quickResponseList.get(position));
    }
  }

  @Override
  public void longClickListener(int position) {

  }

  private void writeYourOwnDialog() {
    EditTextDialog editTextDialog = new EditTextDialog(context);
    editTextDialog.setTitle(context.getString(R.string.write_your_own));
    editTextDialog.setOnClickListener((dialog, whichButton) -> {
      String text = editTextDialog.getTypedString();
      if (text == null) return;
      if (whichButton == BUTTON_POSITIVE) {
        if (text.trim().equals("")) {
          Toast.makeText(context, context.getString(R.string.error_write_something), Toast.LENGTH_SHORT).show();
          return;
        }

        dialog.dismiss();
        dismiss();

        if (dialogListener != null)
          dialogListener.onRefuseWithMessageChoice(text);
      } else {
        dialog.dismiss();
        show();
      }
    });
    editTextDialog.setOnCancelListener(dialog -> show());
    editTextDialog.build();
    editTextDialog.show();
    hide();
  }

  public interface RefuseWithMessageDialogChoiceListener {
    void onRefuseWithMessageChoice(String message);
  }
}
