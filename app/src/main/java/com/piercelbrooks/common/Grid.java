
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;

public abstract class Grid <T extends View> extends LinearLayout
{
    public class Slot extends LinearLayout implements View.OnClickListener, View.OnFocusChangeListener
    {
        private Grid owner;
        private T view;
        private int column;
        private int row;

        public Slot(@NonNull Context context, @NonNull T view, int column, int row, @NonNull Grid owner)
        {
            super(context);
            this.owner = owner;
            this.view = view;
            this.column = column;
            this.row = row;
            addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            view.setOnClickListener(this);
            view.setOnFocusChangeListener(this);
        }

        public Grid getOwner()
        {
            return owner;
        }

        public T getView()
        {
            return view;
        }

        public int getColumn()
        {
            return column;
        }

        public int getRow()
        {
            return row;
        }

        @Override
        public void onClick(View view)
        {
            if (view == this.view)
            {
                owner.onSlotClick(this);
            }
        }

        @Override
        public void onFocusChange(View view, boolean hasFocus)
        {
            if (hasFocus)
            {
                owner.onSlotFocus(this);
            }
        }
    }

    private static final String TAG = "PLB-Grid";

    private Slot focus;
    private ArrayList<Slot> slots;
    private GridListener<T> listener;

    protected abstract void onInitialize(Context context);
    protected abstract T getNewSlot(Context context, int column, int row);
    protected abstract float getSlotWidth(int column, int row);
    protected abstract float getSlotHeight(int column, int row);
    protected abstract int getSlotsPerRow();
    protected abstract int getSlotsPerColumn();

    public Grid(Context context)
    {
        super(context);
        initialize(context);
    }

    public Grid(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        initialize(context);
    }

    public Grid(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public Grid(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context);
    }

    public void onSlotClick(@NonNull Slot slot)
    {
        if (listener != null)
        {
            listener.onClick(slot);
        }
    }

    public void onSlotFocus(@NonNull Slot slot)
    {
        if (listener != null)
        {
            listener.onFocus(slot, focus);
        }
        focus = slot;
    }

    public void setListener(GridListener<T> listener)
    {
        this.listener = listener;
    }

    public int getSlotCount()
    {
        return getSlotsPerColumn()*getSlotsPerRow();
    }

    public int getIndex(int column, int row)
    {
        return (getSlotsPerRow()*row)+column;
    }

    public Slot getSlot(int index)
    {
        if (index < 0)
        {
            return null;
        }
        if (index >= slots.size())
        {
            return null;
        }
        return slots.get(index);
    }

    public Slot getSlot(int column, int row)
    {
        return getSlot(getIndex(column, row));
    }

    private void initialize(Context context)
    {
        float width = 0.0f;
        float height = 0.0f;
        int index;
        LinearLayout rows;
        LinearLayout columns;
        LayoutParams rowParams;
        LayoutParams columnParams;
        Slot slot;
        focus = null;
        slots = new ArrayList<>();
        rows = new LinearLayout(context);
        rows.setOrientation(LinearLayout.VERTICAL);
        rows.setWeightSum(0.0f);
        for (int row = 0; row != getSlotsPerColumn(); ++row)
        {
            rowParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
            rowParams.weight = height;
            columns = new LinearLayout(context);
            columns.setOrientation(LinearLayout.HORIZONTAL);
            columns.setWeightSum(0.0f);
            for (int column = 0; column != getSlotsPerRow(); ++column)
            {
                columnParams = new LayoutParams(0, LayoutParams.MATCH_PARENT);
                columnParams.weight = getSlotWidth(column, row);
                width += columnParams.weight;
                height = getSlotHeight(column, row);
                if (height > rowParams.weight)
                {
                    rowParams.weight = height;
                }
                slot = new Slot(context, getNewSlot(context, column, row), column, row, this);
                slots.add(slot);
                columns.addView(slot, columnParams);
            }
            columns.setWeightSum(width);
            rows.setWeightSum(rowParams.weight + rows.getWeightSum());
            rows.addView(columns, rowParams);
            width = 0.0f;
            height = 0.0f;
        }
        for (int row = 0; row != getSlotsPerColumn(); ++row)
        {
            for (int column = 0; column != getSlotsPerRow(); ++column)
            {
                index = getIndex(column, row);
                slot = slots.get(index);
                if (column > 0)
                {
                    slot.setNextFocusLeftId(slots.get(getIndex(column-1, row)).getId());
                }
                if (row > 0)
                {
                    slot.setNextFocusUpId(slots.get(getIndex(column, row-1)).getId());
                }
                if (column < getSlotsPerColumn()-1)
                {
                    slot.setNextFocusRightId(slots.get(getIndex(column+1, row)).getId());
                }
                if (row < getSlotsPerColumn()-1)
                {
                    slot.setNextFocusDownId(slots.get(getIndex(column, row+1)).getId());
                }
                if (index < slots.size()-1)
                {
                    slot.setNextFocusForwardId(slots.get(index+1).getId());
                }
            }
        }
        rows.setGravity(Gravity.CENTER);
        addView(rows, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        requestLayout();
        listener = null;
        onInitialize(context);
    }
}
