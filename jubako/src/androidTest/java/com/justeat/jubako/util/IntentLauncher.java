package com.justeat.jubako.util;

import android.app.Instrumentation;
import android.content.Intent;
import android.util.Log;
import androidx.test.InstrumentationRegistry;

public class IntentLauncher {

    private static final String TAG = IntentLauncher.class.getSimpleName();


    public static void launchActivityFromIntent(Intent intent) {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Log.d(TAG, String.format("Launching from intent %s", intent.toString()));

        startActivityWithIntent(instrumentation, intent);
    }

    private static void startActivityWithIntent(Instrumentation instrumentation, Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        instrumentation.startActivitySync(intent);
        instrumentation.waitForIdleSync();
    }

}
