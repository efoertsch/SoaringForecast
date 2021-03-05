package org.soaringforecast.rasp.one800wxbrief;

import android.os.Bundle;

import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.common.MasterFragment;
import org.soaringforecast.rasp.databinding.WxBriefRequestView;
import org.soaringforecast.rasp.repository.AppRepository;

import javax.inject.Inject;

public class WxBriefDefaultsFragment extends MasterFragment {
    private WxBriefViewModel wxBriefViewModel;
    private WxBriefRequestView wxBriefRequestView;

    @Inject
    AppRepository appRepository;

    @Inject
    AppPreferences appPreferences;
    private boolean firstTime = true;


    public static WxBriefDefaultsFragment newInstance() {
        WxBriefDefaultsFragment wxBriefDefaultsFragment = new WxBriefDefaultsFragment();
        return wxBriefDefaultsFragment;
    }


}
