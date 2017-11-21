package com.demo.takehometest.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.NoSuchAlgorithmException;

/**
 * This class is used to store common fields in @{@link SharedPreferences} and retrieving them.
 */

public class PreferencesUtil {

    /**
     * Name for the shared preferences
     */
    private static final String MY_PREFERENCES = "my_preferences";

    /**
     * Keys for fields stored in @{@link SharedPreferences}.
     */
    private static final String KEY_TRACKING_ON = "key_tracking_on";
    private static final String KEY_SAFE_ROOM = "key_safe_room";


    private SharedPreferences mSharedPreferences;

    public PreferencesUtil(Context context) {
        mSharedPreferences = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
    }

    /**
     * Check if tracking was enabled.
     *
     * @return true if tracking enabled, false if disabled.
     */
    public boolean isTrackingOn() {
        return mSharedPreferences.getBoolean(KEY_TRACKING_ON, false);
    }

    /**
     * Set tracking on or off.
     *
     * @param flag true for enabling, false for disabling.
     */
    public void setTracking(boolean flag) {
        mSharedPreferences.edit().putBoolean(KEY_TRACKING_ON, flag).apply();
    }

    /**
     * Return key for securing Room data
     *
     * @return key as char array
     */
    public char[] getKeySafeRoom() {
        String key = mSharedPreferences.getString(KEY_SAFE_ROOM, "");
        if (key.equals("")) {
            try {
                key = AppMethods.generateKey();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                key = AppMethods.getRandomString(AppConstants.KEY_LENGTH);
            }
            saveKey(key);
            return key.toCharArray();
        } else {
            return key.toCharArray();
        }
    }

    /**
     * Save key in preferences
     *
     * @param key Key value
     */
    private void saveKey(String key) {
        mSharedPreferences.edit().putString(KEY_SAFE_ROOM, key).apply();
    }

}