package com.volynski.familytrack.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.RemoteViews;
import com.google.gson.Gson;
import com.volynski.familytrack.R;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.views.MainActivity;

import java.io.IOException;
import timber.log.Timber;

/**
 * Created by DmitryVolynski on 03.11.2017.
 */

public class FamilyTrackWidgetProvider extends AppWidgetProvider {
    private static RemoteViews mViews;

    static void updateAppWidget(Context context,
                                AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        mViews = getRecipeRemoteView(context);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, mViews);
    }

    private static RemoteViews getRecipeRemoteView(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_list);

        Intent intent = new Intent(context, FamilyTrackWidgetService.class);
        views.setRemoteAdapter(R.id.listview_widget_users, intent);

        Intent appIntent = new Intent(context, MainActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0,
                appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.listview_widget_users, appPendingIntent);

        views.setEmptyView(R.id.listview_widget_users, R.id.textview_widget_empty);
        return views;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        ComponentName componentName = new ComponentName(context, this.getClass());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listview_widget_users);
    }

    @Override
    public void onUpdate(Context context,
                         AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}


