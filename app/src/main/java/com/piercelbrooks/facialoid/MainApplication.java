
// Author: Pierce Brooks

package com.piercelbrooks.facialoid;

import android.app.Activity;
import androidx.annotation.DrawableRes;

import com.google.firebase.FirebaseApp;
import com.piercelbrooks.common.BasicApplication;

import java.io.File;

public class MainApplication extends BasicApplication
{
    private static final String TAG = "OID-MainApp";

    private MainActivity activity;

    public String getDataPath() {
        String path = getApplicationInfo().dataDir;
        if (path.length() != 0) {
            if (path.charAt(path.length()-1) != File.separatorChar) {
                path += File.separatorChar;
            }
        }
        return path;
    }

    @Override
    public MainActivity getActivity() {
        return activity;
    }

    @Override
    public @DrawableRes int getEmptyDrawable() {
        return R.drawable.empty;
    }

    @Override
    protected void create() {
        FirebaseApp.initializeApp(this);
        activity = null;
    }

    @Override
    protected void terminate() {

    }

    @Override
    protected void activityCreated(Activity activity) {

    }

    @Override
    protected void activityDestroyed(Activity activity) {

    }

    @Override
    protected void activityStarted(Activity activity) {

    }

    @Override
    protected void activityStopped(Activity activity) {

    }

    @Override
    protected void activityResumed(Activity activity) {
        if (activity != null) {
            if (activity instanceof MainActivity) {
                this.activity = (MainActivity) activity;
            }
        }
    }

    @Override
    protected void activityPaused(Activity activity) {
        if (activity == this.activity) {
            this.activity = null;
        }
    }

    @Override
    public Class<?> getCitizenClass() {
        return MainApplication.class;
    }
}