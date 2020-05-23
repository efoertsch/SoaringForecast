package org.soaringforecast.rasp.turnpoints;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.soaringforecast.rasp.common.CheckBeforeGoingBack;
import org.soaringforecast.rasp.common.MasterActivity;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.turnpoints.airnav.AirNavActivity;
import org.soaringforecast.rasp.turnpoints.edit.TurnpointEditFragment;
import org.soaringforecast.rasp.turnpoints.list.TurnpointListFragment;
import org.soaringforecast.rasp.turnpoints.messages.DisplayAirNav;
import org.soaringforecast.rasp.turnpoints.messages.SendEmail;
import org.soaringforecast.rasp.turnpoints.messages.TurnpointSearchForEdit;
import org.soaringforecast.rasp.turnpoints.search.TurnpointSearchForEditFragment;
import org.soaringforecast.rasp.turnpoints.seeyou.SeeYouImportFragment;
import org.soaringforecast.rasp.turnpoints.turnpointview.TurnpointSatelliteViewFragment;

import java.util.List;

import javax.inject.Inject;

import androidx.fragment.app.Fragment;
import timber.log.Timber;

public class TurnpointActivity extends MasterActivity {

    private static final String TURNPOINT_OP = "TURNPOINT_OP";
    private static final String DISPLAY_TURNPOINTS = "DISPLAY_TURNPOINTS";
    private static final String TURNPOINT_EDIT = "TURNPOINT_EDIT";
    private static final String TURNPOINT_SATELLITE_VIEW = "TURNPOINT_SATELLITE_VIEW";
    private static final String TURNPOINT_IMPORT = "TURNPOINT_IMPORT";
    private static final String TURNPOINT = "TURNPOINT";


    @Inject
    public AppRepository appRepository;

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
                case TURNPOINT_EDIT:
                    return getTurnpointEditFragment();
                case TURNPOINT_SATELLITE_VIEW:
                    return getTurnpointSatelliteFragment();
                default:
                    return getTurnpointEditFragment();
            }
        }
        return null;

    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0 || getSupportFragmentManager().getFragments().size() > 0) {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            if (fragments.get(fragments.size() - 1) instanceof CheckBeforeGoingBack) {
                CheckBeforeGoingBack checkBeforeGoingBack = (CheckBeforeGoingBack) fragments.get(fragments.size() - 1);
                if (!checkBeforeGoingBack.okToGoBack()) {
                    return;
                }
            }
        }
        popCurrentFragment();
    }

    private Fragment getTurnpointListFragment() {
        return TurnpointListFragment.newInstance();
    }

    private Fragment getSeeYouImportFragment() {
        return SeeYouImportFragment.newInstance();
    }

    private Fragment getTurnpointEditFragment() {
        Turnpoint turnpoint = getIntent().getExtras().getParcelable(TURNPOINT);
        return TurnpointEditFragment.newInstance(turnpoint);
    }

    private Fragment getTurnpointSatelliteFragment() {
        Turnpoint turnpoint = getIntent().getExtras().getParcelable(TURNPOINT);
        return TurnpointSatelliteViewFragment.newInstance(turnpoint);
    }


    // Bus messages
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TurnpointSearchForEdit event) {
        displayFragment(TurnpointSearchForEditFragment.newInstance(), false, true);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DisplayAirNav displayAirNav) {
        //displayFragment(AirNavFragment.newInstance(displayAirNav.getTurnpoint().getCode()), false, true);
        startActivity(AirNavActivity.Builder.getBuilder().setAirportCode(displayAirNav.getTurnpoint().getCode()).build(this));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTurnpointEmailIntent(SendEmail sendEmail) {
        try {
            startActivity(sendEmail.getIntent());
        } catch (Exception e) {
            Timber.e(e);
        }
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

        public TurnpointActivity.Builder importTurnpoints() {
            bundle.putString(TURNPOINT_OP, TURNPOINT_IMPORT);
            return this;
        }

        public TurnpointActivity.Builder editTurnpoint(Turnpoint turnpoint) {
            bundle.putString(TURNPOINT_OP, TURNPOINT_EDIT);
            bundle.putParcelable(TURNPOINT, turnpoint);
            return this;
        }

        public TurnpointActivity.Builder displayTurnpointSatelliteView(Turnpoint turnpoint) {
            bundle.putString(TURNPOINT_OP, TURNPOINT_SATELLITE_VIEW);
            bundle.putParcelable(TURNPOINT, turnpoint);
            return this;
        }

        public Intent build(Context context) {
            Intent intent = new Intent(context, TurnpointActivity.class);
            intent.putExtras(bundle);
            return intent;
        }
    }
}
