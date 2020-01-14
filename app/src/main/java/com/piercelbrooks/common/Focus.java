
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.content.Context;
import androidx.annotation.Nullable;
import android.view.View;

public class Focus extends ContextWrap
{
    private static final String TAG = "PLB-Focus";

    private View view;

    public Focus(Context context)
    {
        super(context);
        this.view = null;
    }

    @Override
    public void onBirth()
    {

    }

    @Override
    public void onDeath()
    {

    }


    @Override
    public Class<?> getCitizenClass() 
    {
        return Focus.class;
    }

    public void setView(@Nullable View view)
    {

    }
}
