package com.example.scanny;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {

    private static final String PREFS_NAME   = "scanny_prefs";
    private static final String KEY_OCR_LANG = "ocr_lang";
    private static final String KEY_THEME    = "app_theme";
    private static final String KEY_APP_LANG = "app_lang";

    public static final String LANG_ENG  = "eng";
    public static final String LANG_RUS  = "rus";
    public static final String LANG_BOTH = "eng+rus";

    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK  = "dark";

    public static final String APP_LANG_EN = "en";
    public static final String APP_LANG_RU = "ru";

    private final SharedPreferences prefs;

    public AppPreferences(Context context) {
        prefs = context.getApplicationContext()
                       .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getOcrLang() { return prefs.getString(KEY_OCR_LANG, LANG_BOTH); }
    public void setOcrLang(String lang) { prefs.edit().putString(KEY_OCR_LANG, lang).apply(); }

    public String getTheme() { return prefs.getString(KEY_THEME, THEME_LIGHT); }
    public void setTheme(String theme) { prefs.edit().putString(KEY_THEME, theme).apply(); }
    public boolean isDark() { return THEME_DARK.equals(getTheme()); }

    public String getAppLang() { return prefs.getString(KEY_APP_LANG, APP_LANG_EN); }
    public void setAppLang(String lang) { prefs.edit().putString(KEY_APP_LANG, lang).apply(); }
    public boolean isRussian() { return APP_LANG_RU.equals(getAppLang()); }
}
