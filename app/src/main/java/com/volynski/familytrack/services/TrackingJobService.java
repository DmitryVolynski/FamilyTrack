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
import com.volynski.familytrack.utils.IntentUtil;
import com.volynski.familytrack.utils.SharedPrefsUtil;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 12.09.2017.
 */

public class TrackingJobService extends JobService {
    public static final String TAG = TrackingJobService.class.getSimpleName();
    private boolean mIsStarted = false;

    @Override
    public boolean onStartJob(final JobParameters job) {
        //Timber.v("onStartJob");
        if (mIsStarted) {
            //Timber.v("Already started");
            return true;
        }

        final String userUuid = IntentUtil.extractValueFromBundle(job.getExtras(),
                StringKeys.CURRENT_USER_UUID_KEY);
        TrackingTask task = new TrackingTask(userUuid, this, new TrackingTaskCallback() {
            @Override
            public void onTaskCompleted(int newInterval) {
                // reschedule service if new interval > 0
                if (newInterval > 0) {
                    //Timber.v("Rescheduling service with new interval=" + newInterval);
                    SharedPrefsUtil.setTrackingInterval(TrackingJobService.this, newInterval);
                    rescheduleService(userUuid, newInterval);
                }
                jobFinished(job, false);
            }
        });
        task.run();
        mIsStarted = true;
        return true;
    }

    private void rescheduleService(String userUuid, int newInterval) {
        FirebaseJobDispatcher dispatcher =
                new FirebaseJobDispatcher(new GooglePlayDriver(this));
        dispatcher.cancel(TrackingJobService.TAG);

        TrackingJobService.startJobService(this, userUuid, 0, newInterval);
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        //Timber.v("onStopJob");
        FirebaseJobDispatcher dispatcher =
                new FirebaseJobDispatcher(new GooglePlayDriver(this));
        dispatcher.cancel(TrackingJobService.TAG);
        mIsStarted = false;
        return false;
    }

    public static void startJobService(Context context, String userUuid,
                                       int windowStart, int windowEnd ) {
        FirebaseJobDispatcher dispatcher =
                new FirebaseJobDispatcher(new GooglePlayDriver(context));

        Bundle bundle = new Bundle();
        bundle.putString(StringKeys.CURRENT_USER_UUID_KEY, userUuid);

        Job trackingJob = dispatcher.newJobBuilder()
                .setTag(TrackingJobService.TAG)
                .setService(TrackingJobService.class)
                .setExtras(bundle)
                .setRecurring(true)
                .setLifetime(Lifetime.FOREVER)
                .setReplaceCurrent(true)
                .setTrigger(Trigger.executionWindow(windowEnd, windowEnd))
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .build();

        dispatcher.mustSchedule(trackingJob);
    }

    public static void stopJobService(Context context) {
        FirebaseJobDispatcher dispatcher =
                new FirebaseJobDispatcher(new GooglePlayDriver(context));
        dispatcher.cancel(TrackingJobService.TAG);
    }
}
