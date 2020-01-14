package com.piercelbrooks.common;

import androidx.annotation.Nullable;
import android.util.Log;

public class Runner extends Thread
{
    private static final String TAG = "PLB-Run";

    private Runnable runnable;
    private String message;

    public Runner(@Nullable Runnable runnable, @Nullable String message)
    {
        this.runnable = runnable;
        this.message = message;
    }

    public Runner(@Nullable Runnable runnable)
    {
        this(runnable, null);
    }

    @Override
    public void run()
    {
        Log.i(TAG, "Running...");
        if (message != null)
        {
            Log.i(TAG, "Message: "+message);
        }
        if (runnable != null)
        {
            runnable.run();
        }
        else
        {
            Log.w(TAG, "No runnable for me ("+Utilities.getIdentifier(this) +")!");
        }
        Log.i(TAG, "Ran!");
    }

    @Override
    public String toString()
    {
        return TAG;
    }

    public Runnable getRunnable()
    {
        return runnable;
    }

    public String getMessage()
    {
        return message;
    }
}
