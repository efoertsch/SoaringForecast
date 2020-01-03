package org.soaringforecast.rasp.turnpoints;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.soaringforecast.rasp.common.CheckBeforeGoingBack;
import org.soaringforecast.rasp.common.MasterActivity;
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

import java.util.List;

public class TurnpointActivity extends MasterActivity {

    private static final String TURNPOINT_OP = "TURNPOINT_OP";
    private static final String DISPLAY_TURNPOINTS = "DISPLAY_TURNPOINTS";
    private static final String TURNPOINT_EDIT = "TURNPOINT_EDIT";
    private static final String TURNPOINT_EDIT_SEARCH = "TURNPOINT_EDIT_SEARCH";
    private static final String TURNPOINT_IMPORT = "TURNPOINT_IMPORT";
    private static final String ENABLE_CLICK_TASK = "ENABLE_CLICK_TASK";
    private static final String TURNPOINT_ID = "TURNPOINT_ID";

    private boolean enableClickTask = false;

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

    private Fragment getTurnpointListFragment(){
        return TurnpointListFragment.newInstance().displayMenuOptions(true);
    }

    private Fragment getSeeYouImportFragment() {
        return SeeYouImportFragment.newInstance();
    }

    private Fragment getTurnpointSearchForEditFragment() {
        return TurnpointSearchForEditFragment.newInstance();
    }

    private Fragment getTurnpointsDownloadFragment() {
        return new TurnpointsDownloadFragment();
    }

    private Fragment getTurnpointEditFragment() {
        long turnpointID = getIntent().getExtras().getLong(TURNPOINT_ID);
        return TurnpointEditFragment.newInstance(turnpointID);
    }

    private Fragment getTurnpointEditFragment(long turnpointId) {
        return TurnpointEditFragment.newInstance(turnpointId);
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
        displayFragment(getTurnpointEditFragment(event.getTurnpointId()), false, true);
    }


    public static class Builder {

        private Bundle bundle;

        private Builder() {
            bundle = new Bundle();
        }

        public static TurnpointActivity.Builder getBuilder() {
            return new TurnpointActivity.Builder();
        }

        public TurnpointActivity.Builder displayTurnpoints(){
            bundle.putString(TURNPOINT_OP, DISPLAY_TURNPOINTS);
            return this;
        }


        public TurnpointActivity.Builder enableClickTask(boolean enableClickTask) {
            bundle.putBoolean(ENABLE_CLICK_TASK, enableClickTask);
            return this;
        }

        public TurnpointActivity.Builder editTurnpoint(long turnpointId) {
            bundle.putLong(TURNPOINT_ID, turnpointId);
            return this;
        }

        public TurnpointActivity.Builder displayTurnpointEditSearch() {
            bundle.putString(TURNPOINT_OP, TURNPOINT_EDIT_SEARCH);
            return this;
        }

        public TurnpointActivity.Builder displayTurnpointImport() {
            bundle.putString(TURNPOINT_OP, TURNPOINT_IMPORT);
            return this;
        }


        public Intent build(Context context) {
            Intent intent = new Intent(context, TurnpointActivity.class);
            intent.putExtras(bundle);
            return intent;
        }
    }
}
