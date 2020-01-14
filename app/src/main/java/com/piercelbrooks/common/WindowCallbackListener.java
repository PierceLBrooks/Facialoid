
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.view.KeyEvent;
import android.view.MotionEvent;

public interface WindowCallbackListener
{
    public void onKey(KeyEvent event);
    public void onTouch(MotionEvent event);
}
