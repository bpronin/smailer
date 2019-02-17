package com.bopr.android.smailer;

import android.content.Context;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bopr.android.smailer.CallProcessorService.startMailService;
import static com.bopr.android.smailer.Settings.KEY_PREF_RESEND_UNSENT;

/**
 * Watches for internet connection status to start resending unsent email.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class JobSchedulerService extends JobService {

    private static final Logger log = LoggerFactory.getLogger("JobSchedulerService");
    private static final String RESEND_JOB_TAG = "smailer-resend";
    private static final int RESEND_JOB_PERIOD = 5 * 60;

    @Override
    public boolean onStartJob(JobParameters params) {
        log.debug("Starting job");
        switch (params.getTag()) {
            case RESEND_JOB_TAG:
                if (isResendEnabled(this)) {
                    startMailService(this);
                }
                break;
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        /* do nothing */
        return false;
    }

    private static boolean isResendEnabled(Context context) {
        return new Settings(context).getBoolean(KEY_PREF_RESEND_UNSENT, true);
    }

    /**
     * Starts or stops the resend service depending on settings
     *
     * @param context context
     */
    public static void toggleResendJob(Context context) {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        if (isResendEnabled(context)) {
            Job job = dispatcher.newJobBuilder()
                    .setService(JobSchedulerService.class)
                    .setTag(RESEND_JOB_TAG)
                    .setRecurring(true)
                    .setTrigger(Trigger.executionWindow(RESEND_JOB_PERIOD, 2 * RESEND_JOB_PERIOD)) /* between 5 and 10 min from now */
                    .setLifetime(Lifetime.FOREVER)
                    .setReplaceCurrent(true)
                    .setConstraints(
                            Constraint.ON_ANY_NETWORK
                    )
                    .build();
            dispatcher.mustSchedule(job);
            log.debug("Enabled");
        } else {
            dispatcher.cancel(RESEND_JOB_TAG);
            log.debug("Disabled");
        }
    }

}
