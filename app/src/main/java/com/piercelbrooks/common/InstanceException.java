
// Author: Pierce Brooks

package com.piercelbrooks.common;

import androidx.annotation.NonNull;
import android.util.Log;

public class InstanceException extends RuntimeException
{
    private static final String TAG = "PLB-InstanceExcept";

    public InstanceException(@NonNull String tag, int limit)
    {
        super("\""+tag+"\" cannot be instantiated more than "+limit+" time(s)!");
        Log.e(TAG, getMessage());
        printStackTrace();
    }
}
