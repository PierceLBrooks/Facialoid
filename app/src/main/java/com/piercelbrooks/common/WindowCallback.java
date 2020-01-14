
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.content.Context;
import androidx.annotation.Nullable;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

public class WindowCallback extends ContextWrap implements Window.Callback {
    private Window.Callback callback;
    private WindowCallbackListener listener;

    public WindowCallback(Context context, Window.Callback callback) {
        super(context);
        this.callback = callback;
        this.listener = null;
    }

    public WindowCallbackListener getListener() {
        return listener;
    }

    public void setListener(WindowCallbackListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBirth() {

    }

    @Override
    public void onDeath() {
        callback = null;
    }

    @Override
    public Class<?> getCitizenClass() {
        return WindowCallback.class;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (listener != null) {
            listener.onKey(event);
        }
        if (callback != null) {
            return callback.dispatchKeyEvent(event);
        }
        return false;
    }

    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        if (callback != null) {
            return callback.dispatchKeyShortcutEvent(event);
        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (listener != null) {
            listener.onTouch(event);
        }
        if (callback != null) {
            return callback.dispatchTouchEvent(event);
        }
        return false;
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        if (callback != null) {
            return callback.dispatchTrackballEvent(event);
        }
        return false;
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        if (callback != null) {
            return callback.dispatchGenericMotionEvent(event);
        }
        return false;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (callback != null) {
            return callback.dispatchPopulateAccessibilityEvent(event);
        }
        return false;
    }

    @Nullable
    @Override
    public View onCreatePanelView(int featureId) {
        if (callback != null) {
            return callback.onCreatePanelView(featureId);
        }
        return null;
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (callback != null) {
            return callback.onCreatePanelMenu(featureId, menu);
        }
        return false;
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        if (callback != null) {
            return callback.onPreparePanel(featureId, view, menu);
        }
        return false;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (callback != null) {
            return callback.onMenuOpened(featureId, menu);
        }
        return false;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (callback != null) {
            return callback.onMenuItemSelected(featureId, item);
        }
        return false;
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams attrs) {
        if (callback != null) {
            callback.onWindowAttributesChanged(attrs);
        }
    }

    @Override
    public void onContentChanged() {
        if (callback != null) {
            callback.onContentChanged();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (callback != null) {
            callback.onWindowFocusChanged(hasFocus);
        }
    }

    @Override
    public void onAttachedToWindow() {
        if (callback != null) {
            callback.onAttachedToWindow();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        if (callback != null) {
            callback.onDetachedFromWindow();
        }
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        if (callback != null) {
            callback.onPanelClosed(featureId, menu);
        }
    }

    @Override
    public boolean onSearchRequested() {
        if (callback != null) {
            return callback.onSearchRequested();
        }
        return false;
    }

    @Override
    public boolean onSearchRequested(SearchEvent searchEvent) {
        if (callback != null) {
            return callback.onSearchRequested(searchEvent);
        }
        return false;
    }

    @Nullable
    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        if (this.callback != null) {
            return this.callback.onWindowStartingActionMode(callback);
        }
        return null;
    }

    @Nullable
    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
        if (this.callback != null) {
            return this.callback.onWindowStartingActionMode(callback, type);
        }
        return null;
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        if (callback != null) {
            callback.onActionModeStarted(mode);
        }
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        if (callback != null) {
            callback.onActionModeFinished(mode);
        }
    }
}
