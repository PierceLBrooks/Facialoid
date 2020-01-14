
// Author: Pierce Brooks

package com.piercelbrooks.common;

import androidx.annotation.Nullable;
import android.util.Log;

public class Assassin extends Thread
{
    private static final String TAG = "PLB-Assassin";

    private Mortal victim;

    public Assassin(@Nullable Mortal victim)
    {
        this.victim = victim;
    }

    @Override
    public void run()
    {
        if (victim == null)
        {
            Log.i(TAG, "No victim for me ("+Utilities.getIdentifier(this)+")!");
        }
        Log.i(TAG, "Assassinating victim ("+Utilities.getIdentifier(victim)+")...");
        victim.death();
        Log.i(TAG, "Assassinated victim ("+Utilities.getIdentifier(victim)+")!");
    }

    @Override
    public String toString()
    {
        return TAG;
    }
}
