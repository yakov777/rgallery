package ua.cn.stu.randomgallery.app.sync;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ua.cn.stu.randomgallery.app.App;
import ua.cn.stu.randomgallery.RandomGalleryClient;

public class SyncService extends JobService {
    private static final String TAG =
            SyncService.class.getSimpleName();
    private static final int JOB_ID = 1;
    private ExecutorService executorService =
            Executors.newSingleThreadExecutor();
    private Future<?> future;
    private SyncState syncState;
    @Override
    public boolean onStartJob(JobParameters params) {
        App app = (App) getApplicationContext();
        syncState = app.getSyncState();
        future = executorService.submit(() -> {
            RandomGalleryClient client = app.getGalleryClient();
            boolean success = false;
            try {
                success = client.syncGallery(percentage -> {
                    syncState.setProgressPercentage(percentage);
                    syncState.notifyListeners();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            }
            if (success) {
                finish(true);
            } else {
                finish(false);
            }
            jobFinished(params, !success);
        });
        syncState.setInProgress(true);
        syncState.notifyListeners();
        return true;
    }
    @Override
    public boolean onStopJob(JobParameters params) {
        future.cancel(true);
        finish(false);
        return false;
    }
    private void finish(boolean success) {
        syncState.setInProgress(false);
        syncState.setScheduled(false);
        syncState.setHasUpdates(!success);
        syncState.notifyListeners();
        if (success) {
            syncState.onSyncFinished();
        } else {
            syncState.onSyncFailed();
        }
    }
    /**
     * Convenient method for launching sync service
     */
    public static void scheduleUpdate(Context context) {
        App app = (App) context.getApplicationContext();
        JobScheduler jobScheduler =
                (JobScheduler) context
                        .getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo jobInfo = new JobInfo.Builder(
                JOB_ID,
                new ComponentName(context, SyncService.class)
        )
                .setRequiredNetworkType(
                        JobInfo.NETWORK_TYPE_UNMETERED)
                .build();
        jobScheduler.schedule(jobInfo);
        app.getSyncState().setScheduled(true);
        app.getSyncState().notifyListeners();
    }
    static boolean isScheduled(Context context) {
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(
                        Context.JOB_SCHEDULER_SERVICE);
        List<JobInfo> jobs = jobScheduler.getAllPendingJobs();
        for (JobInfo job : jobs) {
            if (job.getId() == JOB_ID) return true;
        }
        return false;
    }
}
