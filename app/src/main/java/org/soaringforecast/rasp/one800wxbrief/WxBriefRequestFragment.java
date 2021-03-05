package org.soaringforecast.rasp.one800wxbrief;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.common.MasterFragment;
import org.soaringforecast.rasp.common.messages.CrashReport;
import org.soaringforecast.rasp.databinding.WxBriefDefaultsView;
import org.soaringforecast.rasp.databinding.WxBriefRequestView;
import org.soaringforecast.rasp.one800wxbrief.options.BriefingOptions;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.WxBriefRequestResponse;
import org.soaringforecast.rasp.repository.AppRepository;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import dagger.android.support.DaggerFragment;

public class WxBriefRequestFragment extends MasterFragment {

    private static final String TASKID = "TASKID";
    private WxBriefViewModel wxBriefViewModel;
    private WxBriefDefaultsView wxBriefDefaultsView;

    @Inject
    AppRepository appRepository;

    @Inject
    AppPreferences appPreferences;

    public static WxBriefRequestFragment newInstance(long taskId) {
        WxBriefRequestFragment wxBriefRequestFragment = new WxBriefRequestFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(TASKID, taskId);
        wxBriefRequestFragment.setArguments(bundle);
        return wxBriefRequestFragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Note viewmodel is shared by activity
        wxBriefViewModel = ViewModelProviders.of(getActivity())
                .get(WxBriefViewModel.class)
                .setAppRepository(appRepository)
                .setAppPreferences(appPreferences)
                ;

    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        wxBriefDefaultsView = DataBindingUtil.inflate(inflater, R.layout.wx_brief_defaults, container, false);
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



    private void displayInfoDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("1800WxBrief")
                .setMessage(getString(R.string.check_to_record_briefing_in_1800wxbrief_system))
                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        alertDialog.setCanceledOnTouchOutside(false);
    }


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
