
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.piercelbrooks.facialoid.R;

import java.util.List;

public abstract class BasicService <T extends BasicService<T>> extends Service implements BasicServiceUser<T>, Citizen
{
    private static final String TAG = "PLB-BaseServe";

    private boolean isForeground;

    protected abstract void create();
    protected abstract void destroy();
    protected abstract BasicServiceBinder<T> getBinder(T service);
    protected abstract String getName();
    protected abstract Integer getNotification();
    protected abstract List<NotificationCompat.Action> getNotificationActions();
    protected abstract Class<?> getActivityClass();
    public abstract String getDescription();

    public BasicService()
    {
        super();
        this.isForeground = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Integer notification = getNotification();
        isForeground = false;
        if (notification != null)
        {
            String description = getDescription();
            if (description != null)
            {
                android.app.Notification build;
                List<NotificationCompat.Action> actions = getNotificationActions();
                String id = TAG+"_"+Constants.NOTIFICATION_CHANNEL;
                PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, BasicActivity.getLauncher(getContext(), getActivityClass()), 0);
                NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), id);
                builder.setContentTitle(getName());
                builder.setContentText(description);
                builder.setContentIntent(pendingIntent);
                builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                builder.setVibrate(new long[]{});
                builder.setSmallIcon(R.drawable.empty);
                if (actions != null)
                {
                    for (int i = 0; i != actions.size(); ++i)
                    {
                        builder.addAction(actions.get(i));
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    NotificationChannel channel = new NotificationChannel(id, getName(), NotificationManager.IMPORTANCE_HIGH);
                    manager.createNotificationChannel(channel);
                }
                build = builder.build();
                startForeground(notification.intValue(), build);
                manager.notify(notification.intValue(), build);
                isForeground = true;
            }
        }
        super.onStartCommand(intent, flags, startId);
        Log.v(TAG, "Started!");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return getBinder((T)this);
    }

    @Override
    public void onRebind(Intent intent)
    {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        birth();
        create();
    }

    @Override
    public void onDestroy()
    {
        if (isForeground)
        {
            stopForeground(true);
        }
        destroy();
        death();
        super.onDestroy();
    }

    @Override
    public T getService()
    {
        return (T)this;
    }

    @Override
    public Class<?> getCitizenClass()
    {
        return getServiceClass();
    }

    @Override
    public Family getFamily()
    {
        return Family.SERVICE;
    }

    @Override
    public void birth()
    {
        Governor.getInstance().register(this);
    }

    @Override
    public void death()
    {
        Governor.getInstance().unregister(this);
    }

    public boolean getIsForeground()
    {
        return isForeground;
    }

    public Context getContext()
    {
        return getApplicationContext();
    }
}
