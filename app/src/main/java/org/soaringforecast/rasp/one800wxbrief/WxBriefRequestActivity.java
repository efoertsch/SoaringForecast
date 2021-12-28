package org.soaringforecast.rasp.one800wxbrief;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.common.MasterActivity;
import org.soaringforecast.rasp.one800wxbrief.messages.ContineWithWxBrief;
import org.soaringforecast.rasp.one800wxbrief.messages.WxBriefShowDefaults;
import org.soaringforecast.rasp.repository.AppRepository;

import javax.inject.Inject;

public class WxBriefRequestActivity extends MasterActivity {

    private static final String WX_BRIEF_OPTION = "WX_BRIEF_OPTION";
    private static final String TASK_ID = "TASK_ID";
    private static final String TASK_NOTAMS = "TASK_NOTAMS";
    private static final String TASK_ROUTE_BRIEFING = "TASK_ROUTE_BRIEFING ";


    @Inject
    public AppRepository appRepository;

    @Inject
    public AppPreferences appPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Fragment createFragment() {
        long taskId;
        if (appPreferences.getWxBriefShowDisclaimer()){
            return getWxBriefDisclaimerFragment();
        }

        String wxBriefOption = getIntent().getExtras().getString(WX_BRIEF_OPTION);
        if (wxBriefOption != null) {
            switch (wxBriefOption) {
                case TASK_NOTAMS:
                    taskId = getIntent().getExtras().getLong(TASK_ID, -1);
                    return getWxBriefRouteNotamsFragment(taskId);
                case TASK_ROUTE_BRIEFING:
                    taskId = getIntent().getExtras().getLong(TASK_ID, -1);
                    return getWxBriefRequestFragment(taskId);
            }
        }
        return getWxBriefDisclaimerFragment();
    }

    private Fragment getWxBriefDisclaimerFragment() {
        return new WxBriefDisclaimerFragment();
    }

    private Fragment getWxBriefDefaultsFragment() {
        return new WxBriefDefaultsFragment();
    }

    private Fragment getWxBriefRouteNotamsFragment(long taskId) {
        return WxBriefRouteNotamsFragment.newInstance(taskId);
    }

    private Fragment getWxBriefRequestFragment(long taskId) {
        return WxBriefRequestFragment.newInstance(taskId);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ContineWithWxBrief contineWithWxBrief) {
       goToNextScreen();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(WxBriefShowDefaults wxBriefShowDefaults) {
        displayFragment(getWxBriefDefaultsFragment(), false, true);
    }

    private void goToNextScreen() {
        long taskId;
        String wxBriefOption = getIntent().getExtras().getString(WX_BRIEF_OPTION);
        if (wxBriefOption != null){
            switch (wxBriefOption){
                case TASK_NOTAMS:
                    taskId = getIntent().getExtras().getLong(TASK_ID, -1);
                    displayFragment(getWxBriefRouteNotamsFragment(taskId),true, true);
                    break;
                case TASK_ROUTE_BRIEFING:
                    taskId = getIntent().getExtras().getLong(TASK_ID, -1);
                    displayFragment(getWxBriefRequestFragment(taskId),true, true);
                    break;
            }

        }
        //finish();
    }

    public static class Builder {

        private Bundle bundle;

        private Builder() {
            bundle = new Bundle();
        }

        public static WxBriefRequestActivity.Builder getBuilder() {
            return new WxBriefRequestActivity.Builder();
        }

        public WxBriefRequestActivity.Builder displayTaskNotams(long taskId) {
            bundle.putString(WX_BRIEF_OPTION, TASK_NOTAMS);
            bundle.putLong(TASK_ID, taskId);
            return this;
        }

        public WxBriefRequestActivity.Builder displayRouteBriefing(long taskId) {
            bundle.putString(WX_BRIEF_OPTION, TASK_ROUTE_BRIEFING);
            bundle.putLong(TASK_ID, taskId);
            return this;
        }


        public Intent build(Context context) {
            Intent intent = new Intent(context, WxBriefRequestActivity.class);
            intent.putExtras(bundle);
            return intent;
        }
    }


}