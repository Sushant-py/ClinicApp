package com.siddhant.nasya.clinicapp;

import android.app.Activity;
import android.content.Intent;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public class LanguageHelper {

    public static void setLocale(Activity activity, String langCode) {
        LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(langCode);
        AppCompatDelegate.setApplicationLocales(appLocales);
        
        // This ensures the transition is smooth by restarting the activity 
        // with a fade animation and clearing the default transition.
        Intent intent = activity.getIntent();
        activity.finish();
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public static void loadLocale() {
        // System handles this automatically with AppCompatDelegate
    }
}
