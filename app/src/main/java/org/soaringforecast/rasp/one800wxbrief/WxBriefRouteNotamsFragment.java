package org.soaringforecast.rasp.one800wxbrief;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
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
import org.soaringforecast.rasp.databinding.WxBriefNotamsView;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.WxBriefRequestResponse;
import org.soaringforecast.rasp.repository.AppRepository;

import javax.inject.Inject;

public class WxBriefRouteNotamsFragment extends MasterFragment {
    private static final String TASKID = "TASKID";
    private WxBriefViewModel viewModel;
    private WxBriefNotamsView wxBriefNotamsView;
    private long taskId = -1;

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

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            taskId = bundle.getLong(TASKID, -1);
        }
        // Note viewmodel is shared by activity
        viewModel = ViewModelProviders.of(requireActivity())
                .get(WxBriefViewModel.class)
                .setRepository(appRepository)
                .setAppPreferences(appPreferences)
                .setTaskId(taskId);

        //setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        wxBriefNotamsView = DataBindingUtil.inflate(inflater, R.layout.wx_brief_route_notams, container, false);
        wxBriefNotamsView.setLifecycleOwner(getViewLifecycleOwner()); // update UI based on livedata changes.
        wxBriefNotamsView.setViewModel(viewModel);
        wxBriefNotamsView.wxBriefCancel.setOnClickListener(v -> getActivity().finish());
        wxBriefNotamsView.wxBriefNotamsAbbreviatedBriefInfo.setOnClickListener(v-> displayInfoDialog(R.string.wxbrief_notams_abbreviated_brief_info));
        wxBriefNotamsView.wxBriefAircraftRegistrationInfo.setOnClickListener(v-> displayInfoDialog(R.string.wxbrief_aircraft_registration_info));
        wxBriefNotamsView.wxBriefAccountNameInfo.setOnClickListener(v-> displayInfoDialog(R.string.wxbrief_account_name_info));

        return wxBriefNotamsView.getRoot();
    }

    public void onViewCreated(View view, Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);
        viewModel.init();
        // Only using this observer so the validation logic in mediator will be fired
//        viewModel.getValidator().observe(getViewLifecycleOwner(), any -> {
//        });

    }

    public void onResume() {
        boolean bypassObserver = false;
        super.onResume();
//        if (appPreferences.getFirstTimeforDefaultsDisplay()) {
//            EventBus.getDefault().post(new WxBriefShowDefaults());
//            bypassObserver = true;
//        }
//        if (appPreferences.getWxBriefShowDisclaimer()) {
//            EventBus.getDefault().post(new WxBriefShowDisclaimer());
//            bypassObserver = true;
//        }

        if (!bypassObserver) {
            // Only using this observer so the validation logic in mediator will be fired
            viewModel.getValidator().observe(getViewLifecycleOwner(), any -> {
            });
            viewModel.startListening();
        }

    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.wxbrief_defaults_menu, menu);
//        super.onCreateOptionsMenu(menu, inflater);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.wxbrief_defaults_menu:
//                EventBus.getDefault().post(new WxBriefShowDefaults());
//                return true;
//            default:
//                break;
//        }
//
//        return false;
//    }

    public void onPause() {
        super.onPause();
        // Only using this observer so the validation logic in mediator will be fired
        viewModel.getValidator().removeObservers(getViewLifecycleOwner());
        viewModel.stopListening();
    }

    public void onDestroyView() {
        super.onDestroyView();
        wxBriefNotamsView = null;
    }

    private void displayInfoDialog( @StringRes int stringResourceId) {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("1800WxBrief")
                .setView(R.layout.wx_brief_info_layout)
                .setMessage(getString(stringResourceId))

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        TextView textView = (TextView) alertDialog.findViewById(R.id.wxbrief_info);
        textView.setText(getText(stringResourceId));
        alertDialog.setCanceledOnTouchOutside(false);
    }


    // Also is in WxBriefRequestFragment - consolidate in superclass?
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(WxBriefRequestResponse wxBriefRequestResponse) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle("1800WxBrief")
                .setMessage(wxBriefRequestResponse.getMessage())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                });
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
