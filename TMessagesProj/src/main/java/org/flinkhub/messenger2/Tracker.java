package org.flinkhub.messenger2;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class Tracker {
    private static Activity activity;
    private static Context ctx;
    private static FirebaseAnalytics mFirebaseAnalytics = null;
    private static String lastScreenName = "";

    public static void createInstance(Activity activity, Context ctx) {
        Tracker.activity = activity;
        Tracker.ctx = ctx;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(ctx);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
    }

    public static void setUserId(int userId) {
        if (mFirebaseAnalytics == null) {
            return;
        }

        mFirebaseAnalytics.setUserId(Integer.toString(userId));
    }

    public static void setScreenName(String screenName) {
        if (screenName == null) {
            return;
        }

        if (screenName.equals(lastScreenName)) {
            return;
        }

        if (mFirebaseAnalytics == null) {
            return;
        }

        lastScreenName = screenName;
        mFirebaseAnalytics.setCurrentScreen(activity, screenName, null /* class override */);
    }

    public static void logEvent(String eventName, Bundle params) {
        if (mFirebaseAnalytics == null) {
            return;
        }

        mFirebaseAnalytics.logEvent(eventName, params);
    }

    public static void logProfileView(int userId, boolean isSelf) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, Integer.toString(userId));
        bundle.putString("is_own_profile", isSelf ?  "true" : "false");
        logEvent("view_profile", bundle);
    }

    public static void logProfileView(int userId) {
        logProfileView(userId, false);
    }

    public static void setUserProperty(String key, String value) {
        if (mFirebaseAnalytics == null) {
            return;
        }

        mFirebaseAnalytics.setUserProperty(key, value);
    }
}
