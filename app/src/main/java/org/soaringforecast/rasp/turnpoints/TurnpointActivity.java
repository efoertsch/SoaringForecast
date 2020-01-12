package org.soaringforecast.rasp.turnpoints;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.soaringforecast.rasp.common.CheckBeforeGoingBack;
import org.soaringforecast.rasp.common.MasterActivity;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.soaring.messages.DisplayTurnpoint;
import org.soaringforecast.rasp.turnpoints.download.TurnpointsDownloadFragment;
import org.soaringforecast.rasp.turnpoints.edit.TurnpointEditFragment;
import org.soaringforecast.rasp.turnpoints.list.TurnpointListFragment;
import org.soaringforecast.rasp.turnpoints.messages.AddTurnpointsToTask;
import org.soaringforecast.rasp.turnpoints.messages.EditTurnpoint;
import org.soaringforecast.rasp.turnpoints.messages.GoToDownloadImport;
import org.soaringforecast.rasp.turnpoints.messages.GoToTurnpointImport;
import org.soaringforecast.rasp.turnpoints.messages.TurnpointSearchForEdit;
import org.soaringforecast.rasp.turnpoints.search.TurnpointSearchForEditFragment;
import org.soaringforecast.rasp.turnpoints.search.TurnpointSearchForTaskFragment;
import org.soaringforecast.rasp.turnpoints.seeyou.SeeYouImportFragment;
import org.soaringforecast.rasp.turnpoints.turnpointview.TurnpointSatelliteViewFragment;

import java.util.List;

import androidx.fragment.app.Fragment;

public class TurnpointActivity extends MasterActivity {

    private static final String TURNPOINT_OP = "TURNPOINT_OP";
    private static final String DISPLAY_TURNPOINTS = "DISPLAY_TURNPOINTS";
    private static final String TURNPOINT_EDIT = "TURNPOINT_EDIT";
    private static final String TURNPOINT_EDIT_SEARCH = "TURNPOINT_EDIT_SEARCH";
    private static final String TURNPOINT_IMPORT = "TURNPOINT_IMPORT";
    private static final String ENABLE_CLICK_TASK = "ENABLE_CLICK_TASK";
    private static final String TURNPOINT = "TURNPOINT";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Fragment createFragment() {
        String turnpointOp = getIntent().getExtras().getString(TURNPOINT_OP);
        if (turnpointOp != null) {
            switch (turnpointOp) {
                case DISPLAY_TURNPOINTS:
                    return getTurnpointListFragment();
                case TURNPOINT_IMPORT:
                    return getSeeYouImportFragment();
                case TURNPOINT_EDIT_SEARCH:
                    return getTurnpointSearchForEditFragment();
                case TURNPOINT_EDIT:
                    return getTurnpointEditFragment();
                default:
                    return getTurnpointEditFragment();
            }
        }
        return null;

    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            if (fragments.get(fragments.size() - 1) instanceof CheckBeforeGoingBack) {
                CheckBeforeGoingBack checkBeforeGoingBack = (CheckBeforeGoingBack) fragments.get(fragments.size() - 1);
                if (!checkBeforeGoingBack.okToGoBack()) {
                    return;
                }
            }
        }
        super.onBackPressed();
    }

    private Fragment getTurnpointListFragment() {
        return TurnpointListFragment.newInstance();
    }

    private Fragment getTurnpointSearchForEditFragment() {
        return TurnpointSearchForEditFragment.newInstance();
    }

    private Fragment getSeeYouImportFragment() {
        return SeeYouImportFragment.newInstance();
    }


    private Fragment getTurnpointsDownloadFragment() {
        return new TurnpointsDownloadFragment();
    }

    private Fragment getTurnpointEditFragment() {
        Turnpoint turnpoint = getIntent().getExtras().getParcelable(TURNPOINT);
        return TurnpointEditFragment.newInstance(turnpoint);
    }

    private Fragment getTurnpointEditFragment(Turnpoint turnpoint) {
        return TurnpointEditFragment.newInstance(turnpoint);
    }

    // Bus messages

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AddTurnpointsToTask event) {
        displayFragment(new TurnpointSearchForTaskFragment(), true, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TurnpointSearchForEdit event) {
        displayFragment(TurnpointSearchForEditFragment.newInstance(), false, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GoToTurnpointImport event) {
        displayFragment(getSeeYouImportFragment(), true, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GoToDownloadImport event) {
        displayFragment(getTurnpointsDownloadFragment(), true, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EditTurnpoint event) {
        displayFragment(getTurnpointEditFragment(event.getTurnpoint()), false, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTurnpointMessageEvent(DisplayTurnpoint displayTurnpoint) {
        TurnpointSatelliteViewFragment turnpointSatelliteViewFragment =
                TurnpointSatelliteViewFragment.newInstance(displayTurnpoint.getTurnpoint());
        displayFragment(turnpointSatelliteViewFragment, false, true);
    }


    public static class Builder {

        private Bundle bundle;

        private Builder() {
            bundle = new Bundle();
        }

        public static TurnpointActivity.Builder getBuilder() {
            return new TurnpointActivity.Builder();
        }

        public TurnpointActivity.Builder displayTurnpoints() {
            bundle.putString(TURNPOINT_OP, DISPLAY_TURNPOINTS);
            return this;
        }

        public Intent build(Context context) {
            Intent intent = new Intent(context, TurnpointActivity.class);
            intent.putExtras(bundle);
            return intent;
        }
    }
}
