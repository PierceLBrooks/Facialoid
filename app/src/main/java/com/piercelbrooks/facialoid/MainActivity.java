
// Author: Pierce Brooks

package com.piercelbrooks.facialoid;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toolbar;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.piercelbrooks.common.BasicActivity;
import com.piercelbrooks.common.Mayor;
import com.piercelbrooks.mlkit.common.preference.SettingsActivity;

public class MainActivity extends BasicActivity<MayoralFamily> {

    private static final String TAG = "OID-MainAct";

    @Override
    protected void create() {

    }

    @Override
    protected void destroy() {

    }

    @Override
    protected void start() {

    }

    @Override
    protected void stop() {

    }

    @Override
    protected void resume() {
        showMain();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(toolbar);
        getActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    protected void pause() {
        //Log.d(TAG, "endService = "+endService());
    }

    @Override
    protected @IdRes int getFragmentSlot() {
        return R.id.fragment_slot;
    }

    @Override
    public @LayoutRes int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    public Class<?> getCitizenClass() {
        return MainActivity.class;
    }

    @Override
    public <T extends Fragment & Mayor<MayoralFamily>> boolean getIsTemporary(@Nullable T fragment) {
        return false;
    }

    @Override
    public <T extends Fragment & Mayor<MayoralFamily>> void preShow(@Nullable T fragment) {

    }

    @Override
    public <T extends Fragment & Mayor<MayoralFamily>> void onShow(@Nullable T fragment) {

    }

    @Override
    public <T extends Fragment & Mayor<MayoralFamily>> T getNewMayor(@Nullable MayoralFamily mayoralFamily) {
        T mayor = null;
        if (mayoralFamily == null) {
            return mayor;
        }
        switch (mayoralFamily) {
            case MAIN:
                mayor = (T)(new MainFragment());
                break;
        }
        return mayor;
    }

    public void showMain() {
        MainFragment fragment = new MainFragment();
        show(fragment);
    }

    public boolean show(@Nullable MayoralFamily mayoralFamily) {
        return show(getNewMayor(mayoralFamily));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.live_preview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra(SettingsActivity.EXTRA_LAUNCH_SOURCE, SettingsActivity.LaunchSource.LIVE_PREVIEW);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
