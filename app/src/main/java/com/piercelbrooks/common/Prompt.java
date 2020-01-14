
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;

public abstract class Prompt extends DialogFragment implements View.OnClickListener, DialogInterface.OnDismissListener
{
    private static final String TAG = "PLB-Prompt";

    private PromptListener listener;
    private AlertDialog dialog;

    public abstract @LayoutRes int getHeaderLayout();
    public abstract @LayoutRes int getBodyLayout();
    public abstract @IdRes int getPositive();
    public abstract @IdRes int getNegative();
    public abstract @IdRes int getNeutral();

    public Prompt()
    {
        super();
        listener = null;
        dialog = null;
    }

    public void setListener(@Nullable PromptListener listener)
    {
        this.listener = listener;
    }

    public boolean show(@Nullable FragmentActivity activity)
    {
        if (activity == null)
        {
            return false;
        }
        activity.getSupportFragmentManager().beginTransaction().add(this, null).commitNowAllowingStateLoss();
        return true;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View header = getHeader(inflater);
        View body = getBody(inflater);
        builder.setCustomTitle(header);
        builder.setView(body);
        builder.setOnDismissListener(this);
        dialog = builder.create();
        dialog.show();
        if (getPositive() != View.NO_ID)
        {
            body.findViewById(getPositive()).setOnClickListener(this);
        }
        else
        {
            Utilities.disable(body.findViewById(getPositive()));
        }
        if (getNegative() != View.NO_ID)
        {
            body.findViewById(getNegative()).setOnClickListener(this);
        }
        else
        {
            Utilities.disable(body.findViewById(getNegative()));
        }
        if (getNeutral() != View.NO_ID)
        {
            body.findViewById(getNeutral()).setOnClickListener(this);
        }
        else
        {
            Utilities.disable(body.findViewById(getNeutral()));
        }
        return dialog;
    }

    @Override
    public void onClick(View view)
    {
        if (view == null)
        {
            return;
        }
        if (listener == null)
        {
            return;
        }
        if (view.getId() == getPositive())
        {
            listener.onPositive(this);
            return;
        }
        if (view.getId() == getNegative())
        {
            listener.onNegative(this);
            return;
        }
        if (view.getId() == getNeutral())
        {
            listener.onNeutral(this);
            return;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
    }

    private View getHeader(@Nullable LayoutInflater inflater)
    {
        if (inflater == null)
        {
            return null;
        }
        return inflater.inflate(getHeaderLayout(), null, false);
    }

    private View getBody(@Nullable LayoutInflater inflater)
    {
        if (inflater == null)
        {
            return null;
        }
        return inflater.inflate(getBodyLayout(), null, false);
    }
}
