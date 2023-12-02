package dev.alenajam.opendialer.feature.settings;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AboutActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_about);

    setSupportActionBar(findViewById(R.id.toolbar));
    if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    TextView version = findViewById(R.id.version);

    try {
      PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      String versionName = pInfo.versionName;
      version.setText(getString(R.string.version, versionName));
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
  }
}
