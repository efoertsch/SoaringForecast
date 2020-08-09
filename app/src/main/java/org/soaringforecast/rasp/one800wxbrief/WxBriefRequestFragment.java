package org.soaringforecast.rasp.one800wxbrief;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.WxBriefViewModel;
import org.soaringforecast.rasp.repository.AppRepository;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import dagger.android.support.DaggerFragment;

public class WxBriefRequestFragment extends DaggerFragment {

    private static final String TASKID = "TASKID";
    private WxBriefViewModel wxBriefViewModel;
    private WxBriefRequestView wxBriefRequestView;

    @Inject
    AppRepository appRepository;


    public static WxBriefRequestFragment newInstance(long taskId){
        WxBriefRequestFragment  wxBriefRequestFragment= new WxBriefRequestFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(TASKID, taskId);
        wxBriefRequestFragment.setArguments(bundle);
        return wxBriefRequestFragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskId = getArguments().getLong(TASKID);
        // Note viewmodel is shared by activity
        wxBriefViewModel = ViewModelProviders.of(getActivity())
                .get(WxBriefViewModel.class)
                .setAppRepository(appRepository)
                .setTaskId(taskId);

    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        wxBriefRequestView = DataBindingUtil.inflate(inflater, R.layout.wx_brief_request_fragment, container, false);
        wxBriefRequestView.setLifecycleOwner(getViewLifecycleOwner()); // update UI based on livedata changes.

}
