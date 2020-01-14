
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.content.Context;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;

public abstract class BasicCalendar <T extends ViewGroup> extends Grid<T> implements GridListener<T>
{
    private static final String TAG = "PLB-BaseCalendar";

    private Integer days = null;
    private Focus focus = null;

    public abstract @ColorRes int getSlotLabelColor();
    public abstract @DrawableRes int getSlotBackground();
    public abstract int getYear();
    public abstract int getMonth();
    public abstract T getNewSlot(Context context);
    public abstract void onClick(int day, int column, int row);

    public BasicCalendar(Context context)
    {
        super(context);
    }

    public BasicCalendar(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
    }

    public BasicCalendar(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    public BasicCalendar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public static int getMonth(String name)
    {
        int month = Calendar.JANUARY;
        while (month <= Calendar.DECEMBER)
        {
            if (getMonthName(month).equalsIgnoreCase(name))
            {
                return month;
            }
            ++month;
        }
        return -1;
    }

    public static String getMonthName(int month)
    {
        switch (month)
        {
            case Calendar.JANUARY:
                return "January";
            case Calendar.FEBRUARY:
                return "February";
            case Calendar.MARCH:
                return "March";
            case Calendar.APRIL:
                return "April";
            case Calendar.MAY:
                return "May";
            case Calendar.JUNE:
                return "June";
            case Calendar.JULY:
                return "July";
            case Calendar.AUGUST:
                return "August";
            case Calendar.SEPTEMBER:
                return "September";
            case Calendar.OCTOBER:
                return "October";
            case Calendar.NOVEMBER:
                return "November";
            case Calendar.DECEMBER:
                return "December";
        }
        return "";
    }

    public static String getDayName(int day)
    {
        switch (day)
        {
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";
            case Calendar.SUNDAY:
                return "Sunday";
        }
        return "";
    }

    public int getDays()
    {
        if (days != null)
        {
            return days;
        }
        GregorianCalendar calendar = new GregorianCalendar(getYear(), getMonth(), 1);
        days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        return days;
    }

    @Override
    public void onClick(@NonNull Slot slot)
    {
        int column = slot.getColumn();
        int row = slot.getRow();
        onClick(getIndex(column, row), column, row);
    }

    @Override
    public void onFocus(@NonNull Slot next, @NonNull Slot previous)
    {
        focus.setView(next);
    }

    @Override
    protected void onInitialize(Context context)
    {
        if (getDays() > getSlotCount())
        {
            throw new RuntimeException("Bad calendar format!");
        }
        setListener(this);
    }

    @Override
    protected T getNewSlot(Context context, int column, int row)
    {
        int index = getIndex(column, row);
        TextView label = new TextView(context);
        T slot = getNewSlot(context);
        T.LayoutParams params = new T.LayoutParams(T.LayoutParams.MATCH_PARENT, T.LayoutParams.MATCH_PARENT);
        slot.addView(label, params);
        slot.setBackground(ContextCompat.getDrawable(context, getSlotBackground()));
        label.setGravity(Gravity.CENTER);
        label.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        label.setTextColor(ContextCompat.getColor(context, getSlotLabelColor()));
        if (index < getDays())
        {
            label.setText(""+(index+1));
        }
        else
        {
            label.setText("");
        }
        slot.requestLayout();
        return slot;
    }

    @Override
    protected float getSlotWidth(int column, int row)
    {
        return 1.0f;
    }

    @Override
    protected float getSlotHeight(int column, int row)
    {
        return 1.0f;
    }

    @Override
    protected int getSlotsPerRow()
    {
        return 8;
    }

    @Override
    protected int getSlotsPerColumn()
    {
        return 4;
    }
}
