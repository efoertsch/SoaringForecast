package org.soaringforecast.rasp.one800wxbrief;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.MasterFragment;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.databinding.WxBriefDisclaimerView;
import org.soaringforecast.rasp.generated.callback.OnClickListener;
import org.soaringforecast.rasp.repository.AppRepository;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class WxBriefDisclaimerFragment extends MasterFragment {
    @Inject
    public AppRepository appRepository;

    private WxBriefViewModel wxBriefViewModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wxBriefViewModel = ViewModelProviders.of(this)
                .get(WxBriefViewModel.class)
                .setRepository(appRepository);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        WxBriefDisclaimerView wxBriefDisclaimerView = DataBindingUtil.inflate(inflater, R.layout.wx_brief_disclaimer, container, false);
        wxBriefDisclaimerView.setLifecycleOwner(this);
        wxBriefDisclaimerView.setViewModel(wxBriefViewModel);
        Button cancelButton = wxBriefDisclaimerView.wxBriefDisclaimerCancel;
        if(cancelButton != null){
            cancelButton.setOnClickListener(v -> getActivity().finish());
        }
        Button continueButton = wxBriefDisclaimerView.wxBriefDisclaimerContinue;
        if(continueButton != null){
            cancelButton.setOnClickListener(v -> sendWxBriefContinue());
        }
        return wxBriefDisclaimerView.getRoot();
    }

    private void sendWxBriefContinue() {
            EventBus.getDefault().post(new ContineWithWxBrief());
        }

    }

}
