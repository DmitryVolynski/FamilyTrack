package com.volynski.familytrack.services;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;

//import com.firebase.jobdispatcher.FirebaseJobDispatcher;
//import com.firebase.jobdispatcher.GooglePlayDriver;
//import com.firebase.jobdispatcher.Job;
//import com.firebase.jobdispatcher.JobParameters;
//import com.firebase.jobdispatcher.JobService;
//import com.firebase.jobdispatcher.Lifetime;
//import com.firebase.jobdispatcher.RetryStrategy;
//import com.firebase.jobdispatcher.Trigger;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.utils.IntentUtil;
import com.volynski.familytrack.utils.SharedPrefsUtil;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 12.09.2017.
 */

public class TrackingService extends JobService {
    public static final String TAG = TrackingService.class.getSimpleName();
    private static final int JOB_ID = 1;

    @Override
    public boolean onStartJob(final JobParameters job) {
        Timber.v("Start");
        final String userUuid =
                IntentUtil.extractValueFromBundle(job.getExtras(), StringKeys.CURRENT_USER_UUID_KEY);
        TrackingTask task = new TrackingTask(userUuid, this, new TrackingTaskCallback() {
            @Override
            public void onTaskCompleted(int newInterval) {
                // reschedule service if new interval > 0
                if (newInterval > 0) {
                    Timber.v("Rescheduling TrackingService with new interval=" + newInterval);
                    SharedPrefsUtil.setTrackingInterval(TrackingService.this, newInterval);
                    //rescheduleService(userUuid, newInterval);
                }
                jobFinished(job, false);
            }
        });
        task.run();
        return true;
    }

/*
    private void rescheduleService(String userUuid, int newInterval) {
        FirebaseJobDispatcher dispatcher =
                new FirebaseJobDispatcher(new GooglePlayDriver(this));
        dispatcher.cancel(TrackingService.TAG);
        TrackingService.startAsFirebaseService(dispatcher, userUuid, 0, newInterval);
    }
*/

    @Override
    public boolean onStopJob(JobParameters job) {
        Timber.v("TrackingService stopped");
        return false;
    }

    public static void startViaJobSheduler(Context context, String userUuid, int period) {

        PersistableBundle bundle = new PersistableBundle();
        bundle.putString(StringKeys.CURRENT_USER_UUID_KEY, userUuid);

        ComponentName componentName = new ComponentName(context, TrackingService.class);
        JobInfo jobInfo = new JobInfo.Builder(TrackingService.JOB_ID, componentName)
                .setMinimumLatency(period * 1000)
                .setExtras(bundle)
                .build();

        JobScheduler scheduler =
                (JobScheduler)context.getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(jobInfo);
        if (resultCode != JobScheduler.RESULT_SUCCESS) {
            Timber.v("Schedule failed");
        }
    }
/*    public static void startAsFirebaseService(FirebaseJobDispatcher dispatcher, String userUuid,
                                              int windowStart, int windowEnd ) {

        Bundle bundle = new Bundle();
        bundle.putString(StringKeys.CURRENT_USER_UUID_KEY, userUuid);

        Job trackingJob = dispatcher.newJobBuilder()
                .setTag(TrackingService.TAG)
                .setService(TrackingService.class)
                .setExtras(bundle)
                .setRecurring(true)
                .setLifetime(Lifetime.FOREVER)
                .setReplaceCurrent(true)
                .setTrigger(Trigger.executionWindow(windowStart, windowEnd))
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .build();

        dispatcher.schedule(trackingJob);
    }*/
}
