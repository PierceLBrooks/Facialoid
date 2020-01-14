
// Author: Pierce Brooks

package com.piercelbrooks.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Registry <T, U, V extends Set<U>, W extends Map<T, V>>
{
    public class Registerable
    {
        private final T key;
        private final U element;

        public Registerable(@NonNull T key, @NonNull U element)
        {
            this.key = key;
            this.element = element;
        }

        T getKey()
        {
            return key;
        }

        U getElement()
        {
            return element;
        }
    }

    private static final String TAG = "PLB-Registry";

    protected abstract V getRegisterableSet();

    private W elements;
    private AtomicInteger population;

    public Registry(@NonNull W elements)
    {
        this.elements = elements;
        this.population = new AtomicInteger(0);
    }

    public U get(@Nullable T key)
    {
        if (key == null)
        {
            return null;
        }
        if (!elements.containsKey(key))
        {
            return null;
        }
        V temp = elements.get(key);
        Iterator<U> iterator = temp.iterator();
        if (!iterator.hasNext())
        {
            return null;
        }
        return iterator.next();
    }

    public boolean empty()
    {
        if (population.get() == 0)
        {
            return true;
        }
        return false;
    }

    public boolean register(@Nullable T key, @Nullable U element)
    {
        if ((key == null) || (element == null))
        {
            return false;
        }
        if (find(key, element))
        {
            return false;
        }
        population.incrementAndGet();
        if (!elements.containsKey(key))
        {
            elements.put(key, getRegisterableSet());
        }
        if (elements.get(key).contains(element))
        {
            return false;
        }
        elements.get(key).add(element);
        return false;
    }

    public boolean unregister(@Nullable T key, @Nullable U element)
    {
        if ((key == null) || (element == null))
        {
            return false;
        }
        if (!find(key, element))
        {
            return false;
        }
        V temp = elements.get(key);
        temp.remove(element);
        if (temp.isEmpty())
        {
            elements.remove(key);
        }
        population.decrementAndGet();
        return true;
    }

    public boolean unregisterAll()
    {
        if (empty())
        {
            return false;
        }
        ArrayList<Registerable> unregisterables = new ArrayList<>();
        Iterator<Map.Entry<T, V>> iteratorOuter = elements.entrySet().iterator();
        Iterator<U> iteratorInner;
        Map.Entry<T, V> entry;
        Registerable registerable;
        while (iteratorOuter.hasNext())
        {
            entry = iteratorOuter.next();
            iteratorInner = entry.getValue().iterator();
            while (iteratorInner.hasNext())
            {
                unregisterables.add(new Registerable(entry.getKey(), iteratorInner.next()));
            }
        }
        for (int i = 0; i != unregisterables.size(); ++i)
        {
            registerable = unregisterables.get(i);
            unregister(registerable.getKey(), registerable.getElement());
        }
        unregisterables.clear();
        return true;
    }

    private boolean find(@NonNull T key, @NonNull U element)
    {
        if (!elements.containsKey(key))
        {
            return false;
        }
        if (!elements.get(key).contains(element))
        {
            return false;
        }
        return true;
    }
}
