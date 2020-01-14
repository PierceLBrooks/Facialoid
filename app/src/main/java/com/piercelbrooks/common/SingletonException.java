
// Author: Pierce Brooks

package com.piercelbrooks.common;

import androidx.annotation.NonNull;

public class SingletonException extends InstanceException
{
    private static final String TAG = "PLB-SingletonExcept";

    public SingletonException(@NonNull String tag)
    {
        super(tag, 1);
    }
}
