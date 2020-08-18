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
    private boolean firstTime = true;


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
        wxBriefRequestView.wxBriefOfficialBriefInfo.setOnClickListener(v -> displayOfficialBriefingDialog());
        wxBriefViewModel.init();

        return wxBriefRequestView.getRoot();
    }

    public void onViewCreated(View view, Bundle savedInstance){
        super.onViewCreated(view, savedInstance);

        setTailoringOptionsSpinnerValues();

        setProductCodesSpinnerValues();

        wxBriefViewModel.getTailoringListUpdatedFlag().observe(getViewLifecycleOwner(), newValue ->{
            if (firstTime) {
                firstTime = false;
            } else {
                setTailoringOptionsSpinnerValues();
            }
        });
    }

    private void setTailoringOptionsSpinnerValues() {
        wxBriefRequestView.wxBriefTailoringOptionsSpinner.setItems(wxBriefViewModel.getTailoringOptionDescriptionsList()
                , wxBriefViewModel.getSelectedTailoringOptions()
                , getString(R.string.select_wx800brief_tailoring_options)
                , selected -> wxBriefViewModel.setSelectedTailoringOptions(selected));
    }

    private void setProductCodesSpinnerValues() {
        wxBriefRequestView.wxBriefProductCodesSpinner.setItems(wxBriefViewModel.getProductCodeDescriptionList()
                , wxBriefViewModel.getSelectedProductCodes()
                , getString(R.string.select_wx800brief_product_options)
                , selected -> wxBriefViewModel.setSelectedProductCodes(selected));
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

    private void displayOfficialBriefingDialog() {
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
                .show();
        alertDialog.setCanceledOnTouchOutside(false);
    }

}
