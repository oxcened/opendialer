package dev.alenajam.opendialer.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import dev.alenajam.opendialer.App;
import dev.alenajam.opendialer.R;
import dev.alenajam.opendialer.adapter.QuickResponseAdapter;
import dev.alenajam.opendialer.helper.SharedPreferenceHelper;
import dev.alenajam.opendialer.view.EditTextDialog;
import dev.alenajam.opendialer.view.MyDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class CustomizeQuickResponsesActivity extends AppCompatActivity implements QuickResponseAdapter.QuickResponseListener {
  private ArrayList<String> quickResponses;
  private RecyclerView recyclerView;
  private SharedPreferences sharedPreferences;
  private Gson gson = new Gson();
  private EditTextDialog dialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_customize_quick_responses);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle(getString(R.string.customize_quick_responses));
      actionBar.setSubtitle(getString(R.string.long_press_delete));
    }

    sharedPreferences = ((App) getApplicationContext()).getAppSharedPreferences();

    recyclerView = findViewById(R.id.recycler_view_quick_responses);
    String json = sharedPreferences.getString(SharedPreferenceHelper.SP_QUICK_RESPONSES, "");
    quickResponses = gson.fromJson(json, new TypeToken<List<String>>() {
    }.getType());

    recyclerView.setAdapter(new QuickResponseAdapter(quickResponses, this, findViewById(R.id.placeholder)));
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
  }

  @Override
  public void clickListener(final int position) {
    EditTextDialog editTextDialog = new EditTextDialog(this);
    editTextDialog.setTitle(getString(R.string.edit_quick_response));
    editTextDialog.setOnClickListener((dialog, whichButton) -> {
      String text = editTextDialog.getTypedString();
      if (text == null) return;
      if (whichButton == MyDialog.BUTTON_POSITIVE) {
        if (text.trim().equals(""))
          Toast.makeText(CustomizeQuickResponsesActivity.this, getString(R.string.error_write_something), Toast.LENGTH_SHORT).show();
        else {
          dialog.dismiss();
          quickResponses.set(position, text);
          if (recyclerView.getAdapter() != null)
            recyclerView.getAdapter().notifyItemChanged(position);
          updateSharedPreferenceQuickResponse();
        }
      } else {
        dialog.dismiss();
      }
    });
    editTextDialog.build();
    editTextDialog.show();
    String text = quickResponses.get(position);
    editTextDialog.setTypedString(text);
  }

  @Override
  public void longClickListener(int position) {
    quickResponses.remove(position);
    if (recyclerView.getAdapter() != null) {
      recyclerView.getAdapter().notifyItemRemoved(position);
      recyclerView.getAdapter().notifyItemRangeChanged(position, quickResponses.size());
    }
    updateSharedPreferenceQuickResponse();
  }

  private void updateSharedPreferenceQuickResponse() {
    sharedPreferences.edit().putString(SharedPreferenceHelper.SP_QUICK_RESPONSES, gson.toJson(quickResponses)).apply();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_customize_quick_responses, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.action_add) {
      EditTextDialog editTextDialog = new EditTextDialog(this);
      editTextDialog.setTitle(getString(R.string.new_quick_response));
      editTextDialog.setOnClickListener((dialog, whichButton) -> {
        String text = editTextDialog.getTypedString();
        if (text == null) return;
        if (whichButton == MyDialog.BUTTON_POSITIVE) {
          if (text.trim().equals(""))
            Toast.makeText(CustomizeQuickResponsesActivity.this, getString(R.string.error_write_something), Toast.LENGTH_SHORT).show();
          else {
            dialog.dismiss();
            quickResponses.add(text);
            if (recyclerView.getAdapter() != null)
              recyclerView.getAdapter().notifyItemInserted(quickResponses.size() - 1);
            updateSharedPreferenceQuickResponse();
          }
        } else {
          dialog.dismiss();
        }
      });
      editTextDialog.build();
      editTextDialog.show();
      return true;
    }
    return false;
  }

  @Override
  protected void onDestroy() {
    if (dialog != null && dialog.isShowing()) dialog.hide();
    super.onDestroy();
  }
}
