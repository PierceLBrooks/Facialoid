
// Author: Pierce Brooks

package com.piercelbrooks.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.HashSet;

public class Governor extends Registry<Family, Citizen, HashSet<Citizen>, HashMap<Family, HashSet<Citizen>>> implements Citizen
{
    private static final String TAG = "PLB-Governor";
    private static Governor instance = null;

    public Governor()
    {
        super(new HashMap<Family, HashSet<Citizen>>());
    }

    @Override
    protected HashSet<Citizen> getRegisterableSet()
    {
        return new HashSet<>();
    }

    public boolean register(@Nullable Citizen citizen)
    {
        if (citizen == null)
        {
            return false;
        }
        return register(citizen.getFamily(), citizen);
    }

    public boolean unregister(@Nullable Citizen citizen)
    {
        if (citizen == null)
        {
            return false;
        }
        return unregister(citizen.getFamily(), citizen);
    }

    public Citizen getCitizen(@NonNull Family family)
    {
        return get(family);
    }

    public void run(@Nullable Runnable runnable)
    {
        new Runner(runnable).start();
    }

    public void assassinate(@Nullable Mortal victim)
    {
        new Assassin(victim).start();
    }

    public static Governor getInstance()
    {
        return instance;
    }

    @Override
    public Family getFamily()
    {
        return Family.GOVERNOR;
    }

    @Override
    public Class<?> getCitizenClass() {
        return Governor.class;
    }

    @Override
    public void birth()
    {
        if (instance != null)
        {
            Utilities.throwSingletonException(TAG);
            return;
        }
        instance = this;
    }

    @Override
    public void death()
    {
        if (instance != this)
        {
            Utilities.throwSingletonException(TAG);
            return;
        }
        instance = null;
    }
}
