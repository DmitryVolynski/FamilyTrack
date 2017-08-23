package com.volynski.familytrack.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.data.models.firebase.GroupUser;
import com.volynski.familytrack.data.models.firebase.User;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by DmitryVolynski on 23.08.2017.
 *
 * Helper class to save|restore current user data
 *
 */

public class AuthUtil {
    /**
     * Reads user data from GoogleSignAccount, creates a User object and
     * serialize it in shared preferences using json format
     * @param context
     * @param account GoogleSignInAccount - an account of current authenticated user
     */
    public static void saveCurrentUserInPrefs(Context context,
                                                    GoogleSignInAccount account) {
        SharedPreferences preferences =
                context.getSharedPreferences(StringKeys.SHARED_PREFS_FILE_KEY, MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(StringKeys.SHARED_PREFS_CURRENT_USER_KEY,
                createUserFromGoogleSignInAccount(account).ToJson());
        editor.commit();
    }

    /**
     * Reads current user data from shared preferences and deserialize them
     * into User object
     * @param context
     * @return User object with current user data
     */
    public static User getCurrentUserFromPrefs(Context context) {
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
    public static User createUserFromGoogleSignInAccount(GoogleSignInAccount account) {
        return new User(account.getFamilyName(), account.getGivenName(),
                account.getPhotoUrl().toString(), account.getEmail(), "", null);
        }

}
