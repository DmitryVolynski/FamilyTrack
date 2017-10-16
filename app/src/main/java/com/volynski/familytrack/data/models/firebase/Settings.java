package com.volynski.familytrack.data.models.firebase;

import com.google.firebase.database.Exclude;

/**
 * Created by DmitryVolynski on 16.10.2017.
 */

public class Settings {
    private boolean mIsTrackingOn;
    private int mLocationUpdateInterval;
    private int mSettingsUpdateInterval;
    private int mAccuracyLevel;
    private boolean mIsSimulationOn;

    public Settings() {}

    public Settings(boolean isTrackingOn,
            int locationUpdateInterval,
            int settingsUpdateInterval,
            int accuracyLevel,
            boolean isSimulationOn) {
        mIsTrackingOn = isTrackingOn;
        mLocationUpdateInterval = locationUpdateInterval;
        mSettingsUpdateInterval = settingsUpdateInterval;
        mAccuracyLevel = accuracyLevel;
        mIsSimulationOn = isSimulationOn;
    }

    public boolean isIsTrackingOn() {
        return mIsTrackingOn;
    }

    public void setIsTrackingOn(boolean isTrackingOn) {
        this.mIsTrackingOn = isTrackingOn;
    }

    public int getLocationUpdateInterval() {
        return mLocationUpdateInterval;
    }

    public void setLocationUpdateInterval(int locationUpdateInterval) {
        this.mLocationUpdateInterval = locationUpdateInterval;
    }

    public int getSettingsUpdateInterval() {
        return mSettingsUpdateInterval;
    }

    public void setSettingsUpdateInterval(int settingsUpdateInterval) {
        this.mSettingsUpdateInterval = settingsUpdateInterval;
    }

    public int getAccuracyLevel() {
        return mAccuracyLevel;
    }

    public void setAccuracyLevel(int accuracyLevel) {
        this.mAccuracyLevel = accuracyLevel;
    }

    public boolean isIsSimulationOn() {
        return mIsSimulationOn;
    }

    public void setIsSimulationOn(boolean isSimulationOn) {
        this.mIsSimulationOn = isSimulationOn;
    }

    /**
     * Default settings
     * @return
     */
    public static Settings getDefaultInstance() {
        return new Settings(false, 5, 15, 1, false);
    }

    @Exclude
    public String getAccuracyDescription() {
        String result = "?";
        switch (mAccuracyLevel) {
            case 0:
                result = "Minimum accuracy and power consumption";
            case 1:
                result = "Medium accuracy and power consumption";
            case 2:
                result = "Maximum accuracy and power consumption";
        }
        return result;
    }
}
