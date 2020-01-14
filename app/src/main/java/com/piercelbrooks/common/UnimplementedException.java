
// Author: Pierce Brooks

package com.piercelbrooks.common;

import androidx.annotation.NonNull;
import android.util.Log;

public class UnimplementedException extends RuntimeException
{
    private static final String TAG = "PLB-UnimplementExcept";

    public UnimplementedException(@NonNull String tag)
    {
        super("\""+tag+"\" is not implemented!");
        Log.e(TAG, getMessage());
        printStackTrace();
    }
}
