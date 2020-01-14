
// Author: Pierce Brooks

package com.piercelbrooks.common;

import androidx.annotation.NonNull;
import android.view.View;

public interface GridListener <T extends View>
{
    public void onClick(@NonNull Grid<T>.Slot slot);
    public void onFocus(@NonNull Grid<T>.Slot next, @NonNull Grid<T>.Slot previous);
}
