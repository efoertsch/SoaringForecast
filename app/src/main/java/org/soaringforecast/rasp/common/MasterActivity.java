package org.soaringforecast.rasp.common;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.messages.PopThisFragmentFromBackStack;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.soaring.messages.DisplayTurnpointSatelliteView;
import org.soaringforecast.rasp.turnpoints.TurnpointActivity;
import org.soaringforecast.rasp.turnpoints.download.TurnpointsDownloadFragment;
import org.soaringforecast.rasp.turnpoints.messages.EditTurnpoint;
import org.soaringforecast.rasp.turnpoints.messages.GoToDownloadImport;
import org.soaringforecast.rasp.turnpoints.messages.GoToTurnpointImport;
import org.soaringforecast.rasp.turnpoints.turnpointview.TurnpointSatelliteViewFragment;

import java.util.List;

import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import dagger.android.support.DaggerAppCompatActivity;

public abstract class MasterActivity extends DaggerAppCompatActivity {

    private ActionBar actionBar;
    private Toolbar toolbar;
    protected CoordinatorLayout rootView;
    protected abstract Fragment createFragment();

    //TODO allow for master/detail or side by side fragments for large screens
    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        rootView = findViewById(R.id.master_activity_coordinator_layout);

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
                // Don't use addToBackStack(null) if first fragment added to container
                // If you do and 'Back' hit, fragment removed but activity continues with blank screen
                fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
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
        InputMethodManager imm = (InputMethodManager) getBaseContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
        Snackbar.make(rootView, message, duration).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PopThisFragmentFromBackStack event) {
        popCurrentFragment();
    }


    // Putting these here because needs to be handled from bith task and turnpoint activities
    // Perhaps should subclass but...
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTurnpointMessageEvent(DisplayTurnpointSatelliteView displayTurnpointSatelliteView) {
        TurnpointSatelliteViewFragment turnpointSatelliteViewFragment = TurnpointSatelliteViewFragment.newInstance(displayTurnpointSatelliteView.getTurnpoint());
        displayFragment(turnpointSatelliteViewFragment, false, true);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GoToTurnpointImport event) {
        startActivity(TurnpointActivity.Builder.getBuilder().importTurnpoints().build(this));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GoToDownloadImport event) {
        displayFragment(TurnpointsDownloadFragment.newInstance(), false, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EditTurnpoint event) {
        // Doing it this way due to  turnpoint edit cupstyle spinner onItemSelected firing when it shouldn't
        // displayFragment(getTurnpointEditFragment(event.getTurnpoint()), false, true);
        startActivity(TurnpointActivity.Builder.getBuilder().editTurnpoint(event.getTurnpoint()).build(this));
    }

    /**
     * Used by fragment when it wants to remove itself
     */
    public void popCurrentFragment() {
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragments = fm.getFragments();
        if (fragments.size() > 1 ) {
            fm.beginTransaction().remove(fragments.get(fragments.size() -1)).commit();
        } else {
            finish();
        }
    }
}
