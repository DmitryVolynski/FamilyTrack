package com.volynski.familytrack.views.navigators;

/**
 * Created by DmitryVolynski on 21.09.2017.
 */

public interface UserOnMapNavigator {
    public void drawMarkers();
    void showUserOnMap(double latitude, double longitude);
}
