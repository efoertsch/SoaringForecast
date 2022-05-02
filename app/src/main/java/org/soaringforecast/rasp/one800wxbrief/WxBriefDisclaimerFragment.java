package org.soaringforecast.rasp.one800wxbrief;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.databinding.DataBindingUtil;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.databinding.WxBriefDisclaimerView;
import org.soaringforecast.rasp.one800wxbrief.messages.ContinueWithWxBrief;
import org.soaringforecast.rasp.repository.AppRepository;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class WxBriefDisclaimerFragment extends DaggerFragment {
    @Inject
    public AppRepository appRepository;
    @Inject
    public AppPreferences appPreferences;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.wx_brief_authorization);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        WxBriefDisclaimerView wxBriefDisclaimerView = DataBindingUtil.inflate(inflater, R.layout.wx_brief_disclaimer, container, false);
        wxBriefDisclaimerView.setLifecycleOwner(this);
        wxBriefDisclaimerView.wxBriefDisclaimerText.setText(appRepository.getWxBriefDisclaimer());
        wxBriefDisclaimerView.wxBriefDisclaimerDoNotShowAgain
                .setOnCheckedChangeListener((buttonView, isChecked) ->
                        appPreferences.setWxBriefShowDisclaimer(!isChecked)
                );

        Button cancelButton = wxBriefDisclaimerView.wxBriefCancel;
        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> getActivity().finish());
        }
        Button continueButton = wxBriefDisclaimerView.wxBriefDisclaimerContinue;
        if (continueButton != null) {
            continueButton.setOnClickListener(v -> sendWxBriefContinue());
        }
        return wxBriefDisclaimerView.getRoot();
    }

    private void sendWxBriefContinue() {
        EventBus.getDefault().post(new ContinueWithWxBrief());
    }

}


