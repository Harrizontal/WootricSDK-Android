package com.wootric.androidsdk.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

import static com.wootric.androidsdk.utils.Constants.NOT_SET;

/**
 * Created by maciejwitowski on 4/10/15.
 */
public class PreferencesUtils {

    private static final String KEY_PREFERENCES = "com.wootric.androidsdk.prefs";
    private static final String KEY_LAST_SEEN = "last_seen";
    private static final String KEY_LAST_SURVEYED = "surveyed";

    private static PreferencesUtils singleton = null;

    private static final long NINETY_DAYS = 1000L *60L *60L *24L *90L;
    private final Context context;

    public static PreferencesUtils getInstance(Context context) {
       if(singleton == null) {
           singleton = new PreferencesUtils(context);
       }

       return singleton;
    }

    private PreferencesUtils(Context context) {
        if(context == null) {
            throw new IllegalArgumentException("Context must not be null.");
        }

        this.context = context;
    }

    private SharedPreferences prefs() {
        return context.getSharedPreferences(KEY_PREFERENCES, Context.MODE_PRIVATE);
    }

    public boolean wasRecentlyLastSeen() {
        return isRecentTime(KEY_LAST_SEEN);
    }

    public boolean wasRecentlySurveyed() {
        return isRecentTime(KEY_LAST_SURVEYED);
    }

    private boolean isRecentTime(String key) {
        long eventTime = prefs().getLong(key, NOT_SET);

        if(eventTime == NOT_SET) {
            return false;
        }

        return new Date().getTime() - eventTime < NINETY_DAYS;
    }

    public long getLastSeen() {
        return prefs().getLong(KEY_LAST_SEEN, NOT_SET);
    }

    public boolean isLastSeenSet() {
        return prefs().getLong(KEY_LAST_SEEN, NOT_SET) != NOT_SET;
    }

    public void setLastSeen(long lastSeen) {
        prefs().edit().putLong(KEY_LAST_SEEN, lastSeen).apply();
    }

    public void setLastSurveyed(long lastSurveyed) {
        prefs().edit().putLong(KEY_LAST_SURVEYED, lastSurveyed).apply();
    }
}
