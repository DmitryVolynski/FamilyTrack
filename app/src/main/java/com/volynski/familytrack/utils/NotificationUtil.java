package com.volynski.familytrack.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.app.NotificationCompat;

import com.volynski.familytrack.R;
import com.volynski.familytrack.data.models.firebase.GeofenceEvent;
import com.volynski.familytrack.views.MainActivity;

import java.util.Calendar;
import java.util.Map;
import java.util.Random;

/**
 * Created by DmitryVolynski on 26.10.2017.
 */

public class NotificationUtil {
    private static final String NOTIFICATION_GROUP_KEY = "FamilyTrack_notifications";
    private static int NOTIFICATION_ID = 12;
    /**
     * Creates a set of notifications based on events parm
     * Every item in events map becomes a separate notification
     * @param events - set of events to create notifications for
     */
    public static void createNotifications(Context context,
                                           Map<String, GeofenceEvent> events) {
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Geofence notification");
        if (events.size() > 0) {
            inboxStyle.setSummaryText(events.size() + " event(s)");
        }

        Intent intent = new Intent(context, MainActivity.class);

        // use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(context,
                (int) System.currentTimeMillis(), intent, 0);

        for (String key : events.keySet()) {
            GeofenceEvent event = events.get(key);
            inboxStyle.addLine(String.format("%1$s entered '%2$s' %3$d",
                    event.getDisplayName(), event.getZone().getName(),
                    Calendar.getInstance().getTimeInMillis()));
        }


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_location)
                        .setGroup(NOTIFICATION_GROUP_KEY)
                        .setStyle(inboxStyle)
                        .addAction(new NotificationCompat.Action(R.mipmap.ic_no_user_photo, "GOT IT", pIntent))
                        .setContentTitle("Geofence notification");

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification n = mBuilder.build();
        n.flags |= Notification.FLAG_GROUP_SUMMARY;
        mNotificationManager.notify(NOTIFICATION_ID, n);
    }
}
