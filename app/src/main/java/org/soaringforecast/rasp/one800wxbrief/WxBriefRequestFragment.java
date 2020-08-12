package org.soaringforecast.rasp.one800wxbrief;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.databinding.WxBriefRequestView;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.Email1800WxBriefRequestResponse;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.WxBriefViewModel;
import org.soaringforecast.rasp.repository.AppRepository;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import dagger.android.support.DaggerFragment;

public class WxBriefRequestFragment extends DaggerFragment {

    private static final String TASKID = "TASKID";
    private WxBriefViewModel wxBriefViewModel;
    private WxBriefRequestView wxBriefRequestView;
    private long taskId;

    @Inject
    AppRepository appRepository;

    @Inject
    AppPreferences appPreferences;


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
        wxBriefViewModel = ViewModelProviders.of(this)
                .get(WxBriefViewModel.class)
                .setAppRepository(appRepository)
                .setAppPreferences(appPreferences)
                .setTaskId(taskId);
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        wxBriefRequestView = DataBindingUtil.inflate(inflater, R.layout.wx_brief_request_fragment, container, false);
        wxBriefRequestView.setLifecycleOwner(getViewLifecycleOwner()); // update UI based on livedata changes.
        wxBriefRequestView.setViewModel(wxBriefViewModel);
        return wxBriefRequestView.getRoot();
    }

    public void onViewCreated(View view, Bundle savedInstance){
        super.onViewCreated(view, savedInstance);
        wxBriefViewModel.init();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Email1800WxBriefRequestResponse email1800WxBriefRequestResponse) {
        String message = email1800WxBriefRequestResponse.getErrorMessage();
        if (message == null) {
            message = (getString(R.string.your_briefing_should_arrive_in_your_mailbox_shortly));
        }

        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("1800WxBrief")
                .setMessage(getString(R.string.your_briefing_should_arrive_in_your_mailbox_shortly))

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        alertDialog.setCanceledOnTouchOutside(false);
    }

}
