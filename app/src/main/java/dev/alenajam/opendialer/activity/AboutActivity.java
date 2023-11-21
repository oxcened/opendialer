package dev.alenajam.opendialer.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import dev.alenajam.opendialer.BuildConfig;
import dev.alenajam.opendialer.R;

public class AboutActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_about);

    setSupportActionBar(findViewById(R.id.toolbar));
    if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    TextView version = findViewById(R.id.version);
    version.setText(getString(R.string.version, BuildConfig.VERSION_NAME));
  }
}
