package com.volynski.familytrack.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.data.models.firebase.User;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by DmitryVolynski on 23.08.2017.
 *
 * Helper class to save|restore current user data in SharedPreferences
 *
 */

public class SharedPrefsUtil {
    /**
     * Saves User object in shared preferences using json format
     * @param context
     * @param user - User object of current authenticated user with Firebase-generated uuid
     */
    public static void setCurrentUserUuid(Context context, String userUuid) {
        SharedPreferences preferences =
                context.getSharedPreferences(StringKeys.SHARED_PREFS_FILE_KEY, MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(StringKeys.SHARED_PREFS_CURRENT_USER_UUID_KEY, userUuid);
        editor.commit();
    }

    /**
     * Reads current user data from shared preferences and deserialize them from json string
     * into User object
     * @param context
     * @return User object with current user data
     */
    public static String getCurrentUserUuid(Context context) {
        SharedPreferences preferences =
                context.getSharedPreferences(StringKeys.SHARED_PREFS_FILE_KEY, MODE_PRIVATE);
        return preferences.getString(StringKeys.SHARED_PREFS_CURRENT_USER_UUID_KEY, "");
    }

    /**
     * Saves User object in shared preferences using json format
     * @param context
     * @param user - User object of current authenticated user with Firebase-generated uuid
     */
    public static void setCurrentUser(Context context,
                                      User user) {
        SharedPreferences preferences =
                context.getSharedPreferences(StringKeys.SHARED_PREFS_FILE_KEY, MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(StringKeys.SHARED_PREFS_CURRENT_USER_KEY,
                user.ToJson());
        editor.commit();
    }

    /**
     * Reads current user data from shared preferences and deserialize them from json string
     * into User object
     * @param context
     * @return User object with current user data
     */
    public static User getCurrentUser(Context context) {
        SharedPreferences preferences =
                context.getSharedPreferences(StringKeys.SHARED_PREFS_FILE_KEY, MODE_PRIVATE);
        String jsonUser = preferences.getString(StringKeys.SHARED_PREFS_CURRENT_USER_KEY, "");
        return User.getInstanceFromJson(jsonUser);
    }

    /**
     * Creates a User object with data from GoogleSignInAccount
     * @param account - GoogleSignInAccount object of authenticated user
     * @return
     */
    public static void setGoogleAccountIdToken(Context context, String idToken) {
        SharedPreferences preferences =
                context.getSharedPreferences(StringKeys.SHARED_PREFS_FILE_KEY, MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(StringKeys.SHARED_PREFS_IDTOKEN_KEY, idToken);
        editor.commit();
    }

    public static void wipeUserData(Context context) {
        SharedPreferences preferences =
                context.getSharedPreferences(StringKeys.SHARED_PREFS_FILE_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(StringKeys.SHARED_PREFS_CURRENT_USER_KEY);
        editor.commit();
    }

    public static String getGoogleAccountIdToken(Context context) {
        SharedPreferences preferences =
                context.getSharedPreferences(StringKeys.SHARED_PREFS_FILE_KEY, MODE_PRIVATE);
        return preferences.getString(StringKeys.SHARED_PREFS_IDTOKEN_KEY, "");
    }
}
