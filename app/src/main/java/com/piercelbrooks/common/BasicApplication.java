
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.DrawableRes;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import android.widget.Toast;

public abstract class BasicApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks, Citizen {
    private static final String TAG = "PLB-BaseApp";

    public abstract @DrawableRes int getEmptyDrawable();
    protected abstract void create();
    protected abstract void terminate();
    protected abstract void activityCreated(Activity activity);
    protected abstract void activityDestroyed(Activity activity);
    protected abstract void activityStarted(Activity activity);
    protected abstract void activityStopped(Activity activity);
    protected abstract void activityResumed(Activity activity);
    protected abstract void activityPaused(Activity activity);

    private Governor governor;
    private Preferences preferences;
    private Activity activity;

    public static BasicApplication getInstance() {
        return (BasicApplication)Governor.getInstance().getCitizen(Family.APPLICATION);
    }

    public Activity getActivity() {
        return activity;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public boolean makeToast(String message) {
        if (message == null) {
            return false;
        }
        if (activity == null) {
            return false;
        }
        activity.runOnUiThread(new Runner(new Runner(null), message) {
            @Override
            public void run() {
                super.run();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, getMessage(), Constants.TOAST_DURATION).show();
                    }
                });
            }
        });
        return true;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
        birth();
        create();
    }

    @Override
    public void onTerminate() {
        terminate();
        unregisterActivityLifecycleCallbacks(this);
        death();
        super.onTerminate();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        activityCreated(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        activityStarted(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        activityResumed(activity);
        this.activity = activity;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        activityPaused(activity);
        if (this.activity == activity) {
            this.activity = null;
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        activityStopped(activity);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        activityDestroyed(activity);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public Family getFamily() {
        return Family.APPLICATION;
    }

    @Override
    public void birth() {
        preferences = new Preferences(this);
        governor = new Governor();
        governor.birth();
        governor.register(this);
    }

    @Override
    public void death() {
        preferences = null;
        governor.unregister(this);
        governor.death();
        governor = null;
    }
}
