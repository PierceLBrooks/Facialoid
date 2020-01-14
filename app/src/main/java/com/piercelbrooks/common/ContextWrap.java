
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.content.Context;
import android.content.ContextWrapper;

public abstract class ContextWrap extends ContextWrapper implements Citizen
{
    protected abstract void onBirth();
    protected abstract void onDeath();

    public ContextWrap(Context base)
    {
        super(base);
        birth();
    }

    @Override
    public Family getFamily()
    {
        return Family.CONTEXT;
    }

    @Override
    public void birth()
    {
        onBirth();
        Governor.getInstance().register(this);
    }

    @Override
    public void death()
    {
        Governor.getInstance().unregister(this);
        onDeath();
    }
}
