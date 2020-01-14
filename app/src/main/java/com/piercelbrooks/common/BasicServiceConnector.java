
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public abstract class BasicServiceConnector <T extends Enum<T>, U extends BasicService<U>> implements ServiceConnection, BasicServiceUser<U>
{
    private static final String TAG = "PLB-BaseServeConnect";

    private BasicServiceActivity<T, U> activity;
    private U service;

    public BasicServiceConnector(BasicServiceActivity<T, U> activity)
    {
        this.activity = activity;
        this.service = null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        if (!name.getClassName().equals(getServiceClass().getName()))
        {
            return;
        }
        BasicServiceBinder<U> binder = (BasicServiceBinder<U>)service;
        Log.d(TAG, "Connecting service...");
        this.service = binder.getService();
        this.activity.onServiceConnected(this.service);
        Log.d(TAG, "Connected service!");
    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {
        if (!name.getClassName().equals(getServiceClass().getName()))
        {
            return;
        }
        Log.d(TAG, "Disconnecting service...");
        this.activity.onServiceDisconnected();
        Log.d(TAG, "Disconnected service!");
    }

    @Override
    public U getService()
    {
        return service;
    }

    public BasicServiceActivity<T, U> getActivity()
    {
        return activity;
    }
}
