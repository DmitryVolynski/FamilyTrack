package com.volynski.familytrack.services;

import android.content.Context;
import android.os.Bundle;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.utils.IntentUtil;
import com.volynski.familytrack.utils.SharedPrefsUtil;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 19.10.2017.

     Алгоритм работы с настройками:
     - при запуске сервиса в SharedPreferences записываются настройки группы в виде json (Group)
     Если настроек нет, записываются стандартные
     - вводится доп.сервис чтения настроек, который обновляет их в SharedPreferences
     - основной сервис работает с учетом настроек

 */

public class SettingsService extends JobService {
    public static final String TAG = SettingsService.class.getSimpleName();

    @Override
    public boolean onStartJob(final JobParameters job) {
        Timber.v("Settings Service started");

        String userUuid =
                IntentUtil.extractValueFromBundle(job.getExtras(), StringKeys.USER_UUID_KEY);

        SettingsTask task = new SettingsTask(userUuid, this, new SettingsTaskCallback() {
            @Override
            public void onTaskCompleted(int newInterval) {
                // reschedule service if new interval > 0
                if (newInterval > 0) {
                    Timber.v("Rescheduling service with new interval=" + newInterval);
                    cancelService();
                    SettingsService.startService(SettingsService.this, newInterval, newInterval);
                }
                jobFinished(job, false);
            }
        });
        task.run();
        return true;
    }

    private void cancelService() {
        FirebaseJobDispatcher dispatcher =
                new FirebaseJobDispatcher(new GooglePlayDriver(this));
        dispatcher.cancel(SettingsService.TAG);
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        Timber.v("Settings Service stopped");
        return false;
    }

    public static void startService(Context context, int windowStart, int windowEnd ) {
        FirebaseJobDispatcher dispatcher =
                new FirebaseJobDispatcher(new GooglePlayDriver(context));

        Job trackingJob = dispatcher.newJobBuilder()
                .setTag(SettingsService.TAG)
                .setService(SettingsService.class)
                .setRecurring(true)
                .setLifetime(Lifetime.FOREVER)
                .setReplaceCurrent(true)
                .setTrigger(Trigger.executionWindow(windowStart, windowEnd))
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .build();

        dispatcher.mustSchedule(trackingJob);
    }
}
