package com.volynski.familytrack.services;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.utils.IntentUtil;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 12.09.2017.
 */

public class TrackingService extends JobService {
    public static final int TRACKING_SERVICE_ID = 12;
    public static final String TAG = TrackingService.class.getSimpleName();

    @Override
    public boolean onStartJob(final JobParameters job) {
        Timber.v("Start");
        String userUuid = IntentUtil.extractValueFromBundle(job.getExtras(), StringKeys.USER_UUID_KEY);
        TrackingTask task = new TrackingTask(userUuid, this, new TrackingTaskCallback() {
            @Override
            public void onTaskCompleted() {
                jobFinished(job, false);
            }
        });
        task.run();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        Timber.v("Stop");
        return false;
    }
}
