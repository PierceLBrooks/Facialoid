
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BasicServiceActivity <T extends Enum<T>, U extends BasicService<U>> extends BasicActivity<T> implements BasicServiceUser<U>
{
    private static final String TAG = "PLB-BaseServeAct";

    private AtomicBoolean isBound;
    private BasicServiceConnector<T, U> connector;
    private U service;

    protected abstract BasicServiceConnector<T, U> getConnector(BasicServiceActivity<T, U> activity);

    public BasicServiceActivity()
    {
        super();
        isBound = new AtomicBoolean();
    }

    public void onServiceConnected(U service)
    {
        if (isBound.get())
        {
            Log.w(TAG, "Already bound.");
            return;
        }
        this.isBound.set(true);
        this.service = service;
    }

    public void onServiceDisconnected()
    {
        if (!isBound.get())
        {
            Log.w(TAG, "No binding.");
            return;
        }
        this.isBound.set(false);
        this.service = null;
    }

    public boolean getIsServiceBound()
    {
        if (connector == null)
        {
            Log.v(TAG, "No connection.");
            return false;
        }
        return isBound.get();
    }

    public boolean getIsServiceRunning()
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (getServiceClass().getName().equals(service.service.getClassName()))
            {
                if (!service.foreground)
                {
                    Log.v(TAG, "Service not foregrounded...");
                    /*if (service.started)
                    {
                        Log.v(TAG, "Service not started...");
                        stopService();
                    }*/
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public boolean beginService()
    {
        if (!getIsServiceRunning())
        {
            Log.d(TAG, "Starting service...");
            startService();
            Log.d(TAG, "Started service!");
        }
        return bindService();
    }

    public boolean endService()
    {
        if (!getIsServiceRunning())
        {
            Log.v(TAG, "No service.");
            return false;
        }
        unbindService();
        return stopService();
    }

    public boolean bindService()
    {
        if (!getIsServiceBound())
        {
            boolean success;
            Log.d(TAG, "Binding service...");
            if (connector == null)
            {
                connector = getConnector(this);
                success = bindService(getServiceIntent(), connector, Context.BIND_AUTO_CREATE);
            }
            else
            {
                success = true;
            }
            Log.d(TAG, "Bound service!");
            return success;
        }
        return true;
    }

    public boolean unbindService()
    {
        if (getIsServiceBound())
        {
            Log.d(TAG, "Unbinding service...");
            if (connector != null)
            {
                unbindService(connector);
                connector = null;
            }
            Log.d(TAG, "Unbound service!");
            return true;
        }
        return false;
    }

    public boolean startService()
    {
        Log.v(TAG, "Starting service...");
        startService(getServiceIntent());
        return true;
    }

    public boolean stopService()
    {
        Log.v(TAG, "Stopping service...");
        return stopService(getServiceIntent());
    }

    public Intent getServiceIntent()
    {
        return new Intent(getApplicationContext(), getServiceClass());
    }

    @Override
    public U getService()
    {
        return service;
    }

    @Override
    protected void onPause()
    {
        if (getIsServiceRunning())
        {
            unbindService();
        }
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        if (getIsServiceRunning())
        {
            bindService();
        }
        super.onResume();
    }
}
