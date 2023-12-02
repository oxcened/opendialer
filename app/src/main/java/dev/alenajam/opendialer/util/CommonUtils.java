package dev.alenajam.opendialer.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

public abstract class CommonUtils {
  public static void setTheme(int mode) {
    AppCompatDelegate.setDefaultNightMode(mode);
  }


  public static float convertDpToPixels(float dp, Context context) {
    return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
  }

  public static int getColorFromAttr(Context context, int attrInt) {
    TypedValue typedValue = new TypedValue();
    context.getTheme().resolveAttribute(attrInt, typedValue, true);
    return ContextCompat.getColor(context, typedValue.resourceId);
  }
}