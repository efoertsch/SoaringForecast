package org.soaringforecast.rasp.common;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.messages.PopThisFragmentFromBackStack;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.soaring.messages.DisplayTurnpoint;
import org.soaringforecast.rasp.turnpointview.IAmDone;
import org.soaringforecast.rasp.turnpointview.TurnpointSatelliteViewFragment;

import dagger.android.support.DaggerAppCompatActivity;

public abstract class MasterActivity extends DaggerAppCompatActivity {

    protected ActionBar actionBar;
    protected Toolbar toolbar;
    private View rootView;

    protected abstract Fragment createFragment();

    //TODO allow for master/detail or side by side fragments for large screens
    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        rootView = findViewById(R.id.task_activity_content);

        // implement this in superclass?
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            setActionBarBackgroundColorToDefault();
        }

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
        if (fragment == null) {
            fragment = createFragment();
            if (fragment != null) {
                fm.beginTransaction().add(R.id.fragmentContainer, fragment)
                        .commit();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void setActionBarBackgroundColorToDefault() {
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        }
    }

    public void setActivityTitle(@StringRes int stringRes) {
        setActivityTitle(getString(stringRes));
    }

    public void setActivityTitle(String title) {
        if (actionBar != null) {
            actionBar.setTitle(title);
        }

        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

   public void displayFragment(Fragment fragment, boolean replace, boolean addToBackstack) {
        FragmentTransaction fragmentTransaction;
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (replace) {
            fragmentTransaction = fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment);
        } else {
            fragmentTransaction = fragmentManager.beginTransaction()
                    .add(R.id.fragmentContainer, fragment);
        }

        if (addToBackstack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SnackbarMessage message) {
        showSnackBarMessage(message.getMessage(), message.getDuration());
    }

    public void showSnackBarMessage(String message, int duration) {
        Snackbar.make(rootView, message, duration).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PopThisFragmentFromBackStack event) {
        popCurrentFragment();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTurnpointMessageEvent(DisplayTurnpoint displayTurnpoint) {
        TurnpointSatelliteViewFragment turnpointSatelliteViewFragment = TurnpointSatelliteViewFragment.newInstance(displayTurnpoint.getTurnpoint());
        displayFragment(turnpointSatelliteViewFragment, false, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIAmDoneMessageEvent(IAmDone iAmDone) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        getSupportFragmentManager().beginTransaction().remove(currentFragment).commit();
    }

    public void popCurrentFragment() {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack();
        if (fm.getFragments().size() == 0){
            finish();
        }
    }
}
