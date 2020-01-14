
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;

public abstract class Text extends androidx.appcompat.widget.AppCompatEditText implements TextWatcher
{
    private class Password implements CharSequence
    {
        private static final String TAG = "PLB-Pass";

        private CharSequence source;

        public Password(CharSequence source)
        {
            this.source = source;
        }

        @Override
        public char charAt(int index)
        {
            return '*';
        }

        @Override
        public int length()
        {
            return source.length();
        }

        @Override
        public CharSequence subSequence(int start, int end)
        {
            return source.subSequence(start, end);
        }
    }

    private class PasswordTransformer extends PasswordTransformationMethod
    {
        private static final String TAG = "PLB-PassTransform";

        public PasswordTransformer()
        {
            super();
        }

        @Override
        public CharSequence getTransformation(CharSequence source, View view)
        {
            return new Password(source);
        }
    }

    private class TextInput extends InputConnectionWrapper
    {
        private static final String TAG = "PLB-TextInput";

        private Text owner;
        private View.OnKeyListener listener;

        public TextInput(Text owner, InputConnection target, boolean mutable)
        {
            super(target, mutable);
            this.owner = owner;
            Log.d(TAG, "Input: "+Utilities.getIdentifier(target));
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event)
        {
            if ((owner != null) && (event != null))
            {
                int action = event.getAction();
                int keyCode = event.getKeyCode();
                switch (keyCode)
                {
                    case KeyEvent.KEYCODE_DEL:
                    case KeyEvent.KEYCODE_FORWARD_DEL:
                        switch (action)
                        {
                            case KeyEvent.ACTION_DOWN:
                                owner.delete(true);
                                break;
                            case KeyEvent.ACTION_UP:
                                owner.delete(false);
                                break;
                        }
                        break;
                }
            }
            return super.sendKeyEvent(event);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength)
        {
            if ((beforeLength == 1) && (afterLength == 0))
            {
                boolean result = true;
                result &= sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                result &= sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
                return result;
            }
            if ((beforeLength == 0) && (afterLength == 1))
            {
                boolean result = true;
                result &= sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_FORWARD_DEL));
                result &= sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_FORWARD_DEL));
                return result;
            }
            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }

    private static final String TAG = "PLB-Text";

    private TextListener listener;
    private String cache;
    private TextInput input;

    protected abstract void onInitialize(Context context);

    public Text(Context context)
    {
        super(context);
        initialize(context);
    }

    public Text(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initialize(context);
    }

    public Text(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context)
    {
        Log.v(TAG, "initialize "+Utilities.getIdentifier(this));
        listener = null;
        cache = null;
        input = null;
        addTextChangedListener(this);
        onInitialize(context);
    }

    public void setListener(TextListener listener)
    {
        this.listener = listener;
    }

    public TextListener getListener()
    {
        return listener;
    }

    public void change()
    {
        if (listener != null)
        {
            listener.onChange(this);
        }
    }

    public void delete(boolean action)
    {
        if (listener != null)
        {
            listener.onDelete(this, action);
        }
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs)
    {
        InputConnection connection = super.onCreateInputConnection(outAttrs);
        if (connection == null)
        {
            input = null;
        }
        else
        {
            input = new TextInput(this, connection, false);
        }
        return input;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {
        cache = getText().toString();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {

    }

    @Override
    public void afterTextChanged(Editable s)
    {
        if (cache == null)
        {
            change();
            return;
        }
        if (!getText().toString().equals(cache))
        {
            change();
            return;
        }
        Log.d(TAG, "No change...");
    }

    @Override
    public void setInputType(int type)
    {
        super.setInputType(type);
        if ((type == EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD) || (type == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD) || (type == EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD))
        {
            setTransformationMethod(new PasswordTransformer());
        }
    }
}
