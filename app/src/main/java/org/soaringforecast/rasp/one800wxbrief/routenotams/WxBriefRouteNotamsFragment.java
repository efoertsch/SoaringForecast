package org.soaringforecast.rasp.one800wxbrief.routenotams;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.common.MasterFragment;
import org.soaringforecast.rasp.common.messages.CrashReport;
import org.soaringforecast.rasp.databinding.WxBriefDefaultsView;
import org.soaringforecast.rasp.one800wxbrief.WxBriefRequestFragment;
import org.soaringforecast.rasp.one800wxbrief.WxBriefViewModel;
import org.soaringforecast.rasp.one800wxbrief.messages.WxBriefShowDefaults;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.WxBriefRequestResponse;
import org.soaringforecast.rasp.repository.AppRepository;

import javax.inject.Inject;

public class WxBriefRouteNotamsFragment extends MasterFragment {
    private static final String TASKID = "TASKID";

    private WxBriefViewModel wxBriefViewModel;
    private WxBriefDefaultsView wxBriefDefaultsView;

    @Inject
    AppRepository appRepository;

    @Inject
    AppPreferences appPreferences;

    public static WxBriefRouteNotamsFragment newInstance(long taskId) {
        WxBriefRouteNotamsFragment wxBriefRouteNotamsFragment = new WxBriefRouteNotamsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(TASKID, taskId);
        wxBriefRouteNotamsFragment.setArguments(bundle);
        return wxBriefRouteNotamsFragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.wx_brief_notams);


        // Note viewmodel is shared by activity
        wxBriefViewModel = ViewModelProviders.of(getActivity())
                .get(WxBriefViewModel.class)
                .setRepository(appRepository)
                .setAppPreferences(appPreferences);
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        wxBriefDefaultsView = DataBindingUtil.inflate(inflater, R.layout.wx_brief_route_notams, container, false);
        wxBriefDefaultsView.setLifecycleOwner(getViewLifecycleOwner()); // update UI based on livedata changes.
        wxBriefDefaultsView.setViewModel(wxBriefViewModel);
        return wxBriefDefaultsView.getRoot();
    }

    public void onViewCreated(View view, Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);
        wxBriefViewModel.init();
        // Only using this observer so the validation logic in mediator will be fired
        wxBriefViewModel.getValidator().observe(getViewLifecycleOwner(), any -> {
        });
    }

    public void onResume() {
        super.onResume();
        if (appPreferences.getFirstTimeforDefaultsDisplay()){
            EventBus.getDefault().post(new WxBriefShowDefaults());
        }
    }


    // Also is in WxBriefRequestFragment - consolidate in superclass?
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(WxBriefRequestResponse wxBriefRequestResponse) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle("1800WxBrief")
                .setMessage(wxBriefRequestResponse.getMessage())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {

                })
                .setNegativeButton(android.R.string.no, null);
        if (wxBriefRequestResponse.isErrorMsg()) {
            builder.setIcon(getResources().getDrawable(R.drawable.ic_baseline_error_outline_24));
        }
        if (wxBriefRequestResponse.getException() != null) {
            builder.setNegativeButton(R.string.report, (dialog, which) -> {
                EventBus.getDefault().post(new CrashReport(wxBriefRequestResponse.getMessage(), wxBriefRequestResponse.getException()));
            });
        }

        AlertDialog alertDialog = builder.show();
        alertDialog.setCanceledOnTouchOutside(false);
    }


}
