package dev.alenajam.opendialer.features.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.PhoneAccount;
import android.telephony.PhoneNumberUtils;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import dev.alenajam.opendialer.App;
import dev.alenajam.opendialer.R;
import dev.alenajam.opendialer.features.dialer.searchContacts.SearchContactsFragment;
import dev.alenajam.opendialer.model.BackPressedListener;
import dev.alenajam.opendialer.model.KeyboardSearchListener;
import dev.alenajam.opendialer.model.OnStatusBarColorChange;
import dev.alenajam.opendialer.model.OpenSearchListener;
import dev.alenajam.opendialer.model.SearchListener;
import dev.alenajam.opendialer.model.SearchOpenChangeListener;
import dev.alenajam.opendialer.model.ToolbarListener;
import dev.alenajam.opendialer.util.CommonUtilsKt;
import dev.alenajam.opendialer.view.SearchView;

public class MainActivity extends AppCompatActivity implements
    ToolbarListener,
    OpenSearchListener,
    KeyboardSearchListener,
    OnStatusBarColorChange {

  public static final String EXTRA_KEY_ADD_CALL = "add_call";
  private NavHostFragment navHostFragment;
  private SearchView searchView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    App app = (App) getApplication();
    app.getApplicationComponent().inject(this);

    setContentView(R.layout.activity_main);

    searchView = findViewById(R.id.searchView);

    navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

    searchView.setOpenListener(isOpen -> {
      Fragment currentFragment = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
      if (currentFragment instanceof SearchOpenChangeListener) {
        ((SearchOpenChangeListener) currentFragment).onOpenChange(isOpen);
      }
    });

    searchView.setTextListener((editText, text) -> {
      Fragment currentFragment = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
      if (currentFragment instanceof BackPressedListener) {
        if (currentFragment instanceof SearchListener) {
          ((SearchListener) currentFragment).onSearch(text);
        }
      }
    });

    handleIntent(getIntent());
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    handleIntent(intent);
  }

  private void handleIntent(Intent intent) {
    if (intent == null) return;

    String action = intent.getAction();
    Uri data = intent.getData();
    boolean hasPhoneData = data != null && PhoneAccount.SCHEME_TEL.equals(data.getScheme());

    if (Intent.ACTION_DIAL.equals(action)) {
      if (hasPhoneData || intent.getBooleanExtra(EXTRA_KEY_ADD_CALL, false)) {
        handleIntentNumber(data);
      }
    }

    if (Intent.ACTION_VIEW.equals(action)) {
      if (hasPhoneData) {
        handleIntentNumber(data);
      }
    }
  }

  private void handleIntentNumber(Uri data) {
    String prefilled = "";
    if (data != null && PhoneAccount.SCHEME_TEL.equals(data.getScheme())) {
      String number = data.getSchemeSpecificPart();
      prefilled = PhoneNumberUtils.convertKeypadLettersToDigits(
          PhoneNumberUtils.replaceUnicodeDigits(number)
      );
    }

    navHostFragment
        .getNavController()
        .popBackStack(R.id.homeFragment, false);

    navHostFragment
        .getNavController()
        .navigate(
            MainFragmentDirections.Companion.actionHomeFragmentToSearchContactsFragment(
                SearchContactsFragment.InitiationType.DIALPAD, prefilled
            )
        );
  }

  @Override
  public void onBackPressed() {
    Fragment currentFragment = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
    if (currentFragment instanceof BackPressedListener) {
      if (!((BackPressedListener) currentFragment).onBackPressed()) {
        return;
      }
    }

    super.onBackPressed();
  }

  @Override
  public void hideToolbar(boolean animate) {
    searchView.setVisibility(View.GONE);
  }

  @Override
  public void showToolbar(boolean animate) {
    searchView.setVisibility(View.VISIBLE);
  }

  @Override
  public void openSearch() {
    searchView.open();
  }

  @Override
  public void closeSearch() {
    searchView.close();
  }

  @Override
  public void closeSearchKeyboard() {
    searchView.closeKeyboard();
  }

  @Override
  public void onColorChange(int color) {
    getWindow().setStatusBarColor(color);
    CommonUtilsKt.updateStatusBarLightMode(getWindow(), color);
  }
}