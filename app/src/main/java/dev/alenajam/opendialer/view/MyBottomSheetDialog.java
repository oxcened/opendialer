package dev.alenajam.opendialer.view;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import dev.alenajam.opendialer.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class MyBottomSheetDialog extends BottomSheetDialog {

  protected MyBottomSheetDialog(Context context) {
    super(context, R.style.CustomBottomSheetDialogTheme);
  }

  protected MyBottomSheetDialog(@NonNull Context context, int viewResId) {
    super(context);
    View sheetView = getLayoutInflater().inflate(viewResId, null);
    setContentView(sheetView);
  }
}
