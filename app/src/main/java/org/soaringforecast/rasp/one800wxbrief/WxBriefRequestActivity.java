package org.soaringforecast.rasp.one800wxbrief;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.common.MasterActivity;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.repository.AppRepository;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

public class WxBriefRequestActivity extends MasterActivity {

    private static final String WX_BRIEF_OPTION = "WX_BRIEF_OPTION";
    private static final String TASK_ID = "TASK_ID";
    private static final String TASK_NOTAMS = "TASK_NOTAMS";
    private static final String WX_BRIEF_DEFAULTS = "WX_BRIEF_DEFAULTS";
    private static final String WX_BRIEF_BRIEFING = "WX_BRIEF_BRIEFING";

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
        if (!appPreferences.doNotDisplayWxBriefDisclaimer()){
            return getWxBriefDisclaimerFragment();
        }

    }

    private Fragment getWxBriefDisclaimerFragment() {
        return new WxBriefDisclaimerFragment();
    }


    private Fragment getWxBriefRequestFragment(long taskId) {
        return WxBriefRequestFragment.newInstance(taskId);
    }

    private Fragment getWxBriefDefaultsFragment(){
        return WxBriefDefaultsFragment.newInstance();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ContineWithWxBrief contineWithWxBrief) {
       goToNextScreen();
    }

    private Fragment goToNextScreen() {
        String wxBriefOption = getIntent().getExtras().getString(WX_BRIEF_OPTION);
        if (wxBriefOption != null){
            switch (wxBriefOption){
                case "TASK_NOTAMS":
                    displayFragment(getWxBriefDefaultsFragment(),true, true);
            }
            return getWxBriefDefaultsFragment();
        }
        long taskId = getIntent().getExtras().getLong(TASK_ID, -1);
        return getWxBriefRequestFragment(taskId);
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
            bundle.putString(WX_BRIEF_OPTION,TASK_NOTAMS);
            bundle.putLong(TASK_ID, taskId);
            return this;
        }

        public WxBriefRequestActivity.Builder displayBriefingOptions(long taskId) {
            bundle.putString(WX_BRIEF_OPTION,WX_BRIEF_BRIEFING);
            bundle.putLong(TASK_ID, taskId);
            return this;
        }

        public WxBriefRequestActivity.Builder displayWxBriefDefaults() {
            bundle.putString(WX_BRIEF_OPTION,WX_BRIEF_DEFAULTS);
            return this;
        }

        public Intent build(Context context) {
            Intent intent = new Intent(context, WxBriefRequestActivity.class);
            intent.putExtras(bundle);
            return intent;
        }
    }


}