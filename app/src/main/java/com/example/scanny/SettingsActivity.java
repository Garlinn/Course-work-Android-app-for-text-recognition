package com.example.scanny;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends BaseActivity {

    private AppPreferences prefs;
    private TextView settingLanguage;
    private TextView settingTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = new AppPreferences(this);
        settingLanguage = findViewById(R.id.settingLanguage);
        settingTheme    = findViewById(R.id.settingTheme);
        TextView settingAbout = findViewById(R.id.settingAbout);

        updateLabels();

        settingLanguage.setOnClickListener(v -> {
            String[] options   = {"English", "Русский"};
            String[] langCodes = {AppPreferences.APP_LANG_EN, AppPreferences.APP_LANG_RU};
            int checked = AppPreferences.APP_LANG_RU.equals(prefs.getAppLang()) ? 1 : 0;

            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialog_language_title))
                    .setSingleChoiceItems(options, checked, (dialog, which) -> {
                        prefs.setAppLang(langCodes[which]);
                        dialog.dismiss();
                        restartApp();
                    })
                    .show();
        });

        settingTheme.setOnClickListener(v -> {
            String[] options    = {getString(R.string.theme_light), getString(R.string.theme_dark)};
            String[] themeCodes = {AppPreferences.THEME_LIGHT, AppPreferences.THEME_DARK};
            int checked = prefs.isDark() ? 1 : 0;

            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialog_theme_title))
                    .setSingleChoiceItems(options, checked, (dialog, which) -> {
                        prefs.setTheme(themeCodes[which]);
                        AppCompatDelegate.setDefaultNightMode(
                                which == 1
                                ? AppCompatDelegate.MODE_NIGHT_YES
                                : AppCompatDelegate.MODE_NIGHT_NO
                        );
                        dialog.dismiss();
                        restartApp();
                    })
                    .show();
        });

        settingAbout.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.dialog_about_title))
                        .setMessage(getString(R.string.about_text))
                        .setPositiveButton(getString(R.string.dialog_ok), null)
                        .show()
        );

        findViewById(R.id.tabHome).setOnClickListener(v -> navigateTo(HomeActivity.class));
        findViewById(R.id.tabHistory).setOnClickListener(v -> navigateTo(HistoryActivity.class));
    }

    private void updateLabels() {
        settingLanguage.setText(prefs.isRussian()
                ? getString(R.string.setting_language_ru)
                : getString(R.string.setting_language_en));
        settingTheme.setText(prefs.isDark()
                ? getString(R.string.setting_theme_dark)
                : getString(R.string.setting_theme_light));
    }
}
