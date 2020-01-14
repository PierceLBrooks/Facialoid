
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BasicFragment <T extends Enum<T>> extends Fragment implements Mayor<T> {
    private static final String TAG = "PLB-BaseFrag";

    public BasicFragment() {
        super();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayout(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        createView(view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.v(TAG, "onDestroyView "+Utilities.getIdentifier(this));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy "+Utilities.getIdentifier(this));
    }

    @Override
    public Family getFamily() {
        return Family.MAYOR;
    }

    @Override
    public Municipality<T> getMunicipality() {
        Activity activity = getActivity();
        if (activity == null) {
            return null;
        }
        if (activity instanceof Municipality) {
            return (Municipality<T>)activity;
        }
        return null;
    }

    @Override
    public void birth() {
        Log.v(TAG, "Birth: "+getMayoralFamily().name());
        Governor.getInstance().register(this);
        onBirth();
    }

    @Override
    public void death() {
        Log.v(TAG, "Death: "+getMayoralFamily().name());
        onDeath();
        Governor.getInstance().unregister(this);
    }

    public void runOnUiThread(Runnable runnable) {
        Municipality<T> municipality = getMunicipality();
        if (municipality == null) {
            return;
        }
        municipality.runOnUiThread(runnable);
    }
}
