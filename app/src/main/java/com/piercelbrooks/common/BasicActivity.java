
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import android.util.Log;
import android.view.Window;

import java.util.ArrayList;

public abstract class BasicActivity <T extends Enum<T>> extends FragmentActivity implements Municipality<T> {
    private class Shower <U extends Enum<U>, V extends Fragment & Mayor<U>> extends Handler {
        private class Runner <W extends Enum<W>, X extends Fragment & Mayor<W>> implements Runnable {
            private BasicActivity activity;
            private X fragment;

            public Runner(@NonNull BasicActivity activity, @Nullable X fragment) {
                this.activity = activity;
                this.fragment = fragment;
            }

            @Override
            public void run() {
                FragmentManager manager = activity.getSupportFragmentManager();
                Fragment activeFragment = activity.getActiveFragment();
                if (manager == null) {
                    return;
                }
                Utilities.closeKeyboard(activity);
                if (activeFragment != null) {
                    if (activeFragment instanceof Citizen) {
                        ((Citizen)activeFragment).death();
                    }
                    manager.beginTransaction().remove(activeFragment).commitNowAllowingStateLoss();
                }
                activity.setActiveFragment(fragment);
                activity.preShow(fragment);
                if (fragment != null) {
                    fragment.birth();
                    manager.beginTransaction().replace(getFragmentSlot(), fragment, null).commitNowAllowingStateLoss();
                }
                activity.postShow(activeFragment, fragment);
            }
        }

        private BasicActivity activity;
        private V fragment;

        public Shower(@NonNull BasicActivity activity, @Nullable V fragment) {
            this.activity = activity;
            this.fragment = fragment;
        }

        public void post() {
            super.post(new Runner<>(activity, fragment));
        }
    }

    private static final String TAG = "PLB-BaseAct";

    private Fragment activeFragment;
    private Window.Callback androidWindowCallback;
    private WindowCallback commonWindowCallback;
    private boolean isShowing;
    private boolean isBacking;
    private ArrayList<T> backStack;
    private ArrayList<Shower<T, ?>> showers;

    protected abstract void create();
    protected abstract void destroy();
    protected abstract void start();
    protected abstract void stop();
    protected abstract void resume();
    protected abstract void pause();
    protected abstract @IdRes int getFragmentSlot();
    protected abstract @LayoutRes int getLayout();

    public Fragment getActiveFragment() {
        return activeFragment;
    }

    public void setActiveFragment(Fragment activeFragment) {
        this.activeFragment = activeFragment;
    }

    private void restart() {
        pause();
        stop();
        start();
        resume();
    }

    private void setContentView() {
        int layout = getLayout();
        Log.d(TAG, "Setting content view (0x"+Utilities.getHex(layout)+")...");
        setContentView(layout);
    }

    private void commonOnCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        birth();
        activeFragment = null;
        androidWindowCallback = null;
        commonWindowCallback = null;
        isShowing = false;
        isBacking = false;
        backStack = new ArrayList<>();
        showers = new ArrayList<>();
        setContentView();
        create();
    }

    private void commonOnSaveInstanceState(Bundle outState, @Nullable PersistableBundle outPersistentState) {

    }

    private void commonOnRestoreInstanceState(Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        commonOnCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        commonOnCreate(savedInstanceState, null);
    }

    @Override
    protected void onDestroy() {
        destroy();
        death();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        start();
    }

    @Override
    protected void onStop() {
        stop();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Window window = getWindow();
        androidWindowCallback = window.getCallback();
        commonWindowCallback = new WindowCallback(this, androidWindowCallback);
        window.setCallback(commonWindowCallback);
        resume();
    }

    @Override
    protected void onPause() {
        pause();
        getWindow().setCallback(androidWindowCallback);
        commonWindowCallback.death();
        commonWindowCallback = null;
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        restart();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        commonOnSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        commonOnSaveInstanceState(outState, null);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        commonOnRestoreInstanceState(savedInstanceState, persistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        commonOnRestoreInstanceState(savedInstanceState, null);
    }

    @Override
    public void onBackPressed() {
        if (!onBack()) {
            super.onBackPressed();
        }
    }

    @Override
    public BasicApplication getOwner() {
        return (BasicApplication)(Governor.getInstance().getCitizen(Family.APPLICATION));
    }

    @Override
    public T popBack() {
        if (backStack.isEmpty()) {
            return null;
        }
        return backStack.remove(backStack.size()-1);
    }

    @Override
    public boolean onBack() {
        if (isBacking) {
            return false;
        }
        if (!show(getNewMayor(popBack()))) {
            return false;
        }
        isBacking = true;
        return true;
    }

    @Override
    public <Y extends Fragment & Mayor<T>> void postShow(@Nullable Y previous, @Nullable Y current) {
        int i = 0;
        if (previous != null) {
            T mayoralFamilyPrevious = previous.getMayoralFamily();
            if (mayoralFamilyPrevious != null) {
                if (!backStack.isEmpty()) {
                    if (current != null) {
                        boolean check = true;
                        T mayoralFamilyCurrent = current.getMayoralFamily();
                        T mayoralFamilyBack = null;
                        if (mayoralFamilyCurrent != null) {
                            for (i = backStack.size(); --i >= 0;) {
                                mayoralFamilyBack = backStack.get(i);
                                if (mayoralFamilyCurrent.equals(mayoralFamilyBack)) {
                                    check = false;
                                    break;
                                }
                            }
                        }
                        if (check) {
                            if ((!isBacking) && (!getIsTemporary(previous)) && (!mayoralFamilyPrevious.equals(backStack.get(backStack.size()-1)))) {
                                backStack.add(mayoralFamilyPrevious);
                            }
                        } else {
                            if (mayoralFamilyBack != null) {
                                for (int j = backStack.size(); --j >= i; ) {
                                    if (backStack.get(j).equals(mayoralFamilyBack)) {
                                        popBack();
                                        break;
                                    }
                                    popBack();
                                }
                            }
                        }
                    }
                } else {
                    if ((!isBacking) && (!getIsTemporary(previous))) {
                        backStack.add(previous.getMayoralFamily());
                    }
                }
            }
        }
        if (isShowing) {
            if (isBacking) {
                isBacking = false;
            }
            if (showers.isEmpty()) {
                isShowing = false;
            } else {
                showers.remove(showers.size()-1).post();
            }
        }
        Log.d(TAG, backStack.toString());
        onShow(current);
    }

    @Override
    public <Y extends Fragment & Mayor<T>> boolean show(@Nullable Y fragment) {
        if (fragment == null) {
            return false;
        }
        Log.d(TAG, "Show: "+fragment.getMayoralFamily().name());
        showers.add(new Shower<>(this, fragment));
        if (!isShowing) {
            isShowing = true;
            showers.remove(showers.size()-1).post();
        }
        return true;
    }

    @Override
    public Family getFamily() {
        return Family.MUNICIPALITY;
    }

    @Override
    public void birth() {
        Governor.getInstance().register(this);
    }

    @Override
    public void death() {
        Governor.getInstance().unregister(this);
    }

    public static Intent getLauncher(Context context, Class<?> activity) {
        Intent launcher = new Intent(context, activity);
        launcher.addCategory(Intent.CATEGORY_LAUNCHER);
        launcher.setAction(Intent.ACTION_MAIN);
        launcher.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return launcher;
    }
}
