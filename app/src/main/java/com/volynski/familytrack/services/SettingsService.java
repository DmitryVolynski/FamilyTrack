package com.volynski.familytrack.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FamilyTrackDbRefsHelper;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.Settings;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.utils.SharedPrefsUtil;

import timber.log.Timber;

/**
 * SettingsService provides a listener service for app settings
 * It will listen to following nodes:
 *      - active group settings (groups/<activeUserGroupUuid>/settings)
 *      - current user membership (registered_users/<userUuid>/memberships)
 * If active group has changed - service will listen to new settings node
 * If settings has changed - service will replace it in SharedPreferences
 */
public class SettingsService extends Service {
    private DatabaseReference mSettingsRef;
    private DatabaseReference mUserRef;
    private FamilyTrackDataSource mDataSource;
    private String mActiveGroupUuid = "";

    public SettingsService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = START_STICKY;

        final String userUuid = (intent.hasExtra(StringKeys.CURRENT_USER_UUID_KEY) ?
                intent.getStringExtra(StringKeys.CURRENT_USER_UUID_KEY) : "");

        if (userUuid.equals("")) {
            Timber.v("UserUuid from SharedPrefs is empty. Can't start settings listener service");
            return result;
        }

        mDataSource = new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(this),this);
        mDataSource.getUserByUuid(userUuid, new FamilyTrackDataSource.GetUserByUuidCallback() {
            @Override
            public void onGetUserByUuidCompleted(FirebaseResult<User> result) {
                final User user = result.getData();
                if (user != null) {
                    if (result.getData() != null && result.getData().getActiveMembership() != null) {
                        mActiveGroupUuid = result.getData().getActiveMembership().getGroupUuid();
                    }
                    createUserListener(userUuid);
                    createSettingsListener(mActiveGroupUuid);
                } else {
                    // remove settings if user not found or doesn't exist as member of any group
                    Timber.v(String.format("User '%1$s' not found. Settings cleared", userUuid));
                    SharedPrefsUtil.removeActiveGroup(SettingsService.this);
                }
            }
        });
        return result;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void createUserListener(final String userUuid) {
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    if (user.getActiveMembership() == null) {
                        SharedPrefsUtil.removeSettings(SettingsService.this);
                        mActiveGroupUuid = "";
                    } else {
                        if (!mActiveGroupUuid.equals(user.getActiveMembership().getGroupUuid())) {
                            mActiveGroupUuid = user.getActiveMembership().getGroupUuid();
                            createSettingsListener(mActiveGroupUuid);
                        }
                    }
                } else {
                    Timber.v("Unexpected error: null user received for key " + userUuid);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mUserRef = mDataSource.getFirebaseConnection()
                .getReference(FamilyTrackDbRefsHelper.userRef(userUuid));

        mUserRef.addValueEventListener(userListener);
    }

    private void createSettingsListener(final String activeGroupUuid) {
        mSettingsRef = null;
        if (!mActiveGroupUuid.equals("")) {
            ValueEventListener settingsListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Settings settings = dataSnapshot.getValue(Settings.class);
                    if (settings != null) {
                        SharedPrefsUtil.setSettings(SettingsService.this, settings);
                    } else {
                        Timber.v("Unexpected error: null settings received for group " + activeGroupUuid);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mSettingsRef = mDataSource.getFirebaseConnection()
                    .getReference(FamilyTrackDbRefsHelper.groupSettingsRef(activeGroupUuid));

            mSettingsRef.addValueEventListener(settingsListener);
        }

    }
}
