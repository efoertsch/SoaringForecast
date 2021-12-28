package org.soaringforecast.rasp.one800wxbrief;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.common.MasterFragment;
import org.soaringforecast.rasp.common.messages.CrashReport;
import org.soaringforecast.rasp.databinding.WxBriefRequestView;
import org.soaringforecast.rasp.one800wxbrief.messages.WxBriefShowDefaults;
import org.soaringforecast.rasp.one800wxbrief.messages.WxBriefShowDisclaimer;
import org.soaringforecast.rasp.one800wxbrief.options.BriefingOptions;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.WxBriefRequestResponse;
import org.soaringforecast.rasp.repository.AppRepository;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

public class WxBriefRequestFragment extends MasterFragment {

    private static final String TASKID = "TASKID";
    private WxBriefViewModel viewModel;
    private WxBriefRequestView wxBriefRequestView;
    private long taskId = -1;
    private int shortAnimationDuration;
    private boolean firstTime = true;

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
        getActivity().setTitle(R.string.wx_brief_route_briefing);

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

        // Retrieve and cache the system's default "short" animation time.
        shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        //setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        wxBriefRequestView = DataBindingUtil.inflate(inflater, R.layout.wx_brief_request_fragment, container, false);
        wxBriefRequestView.setLifecycleOwner(getViewLifecycleOwner()); // update UI based on livedata changes.
        wxBriefRequestView.setViewModel(viewModel);
        wxBriefRequestView.wxBriefCancel.setOnClickListener(v -> getActivity().finish());

        wxBriefRequestView.wxBriefAircraftRegistrationInfo.setOnClickListener(v-> displayInfoDialog(R.string.wxbrief_aircraft_registration_info));
        wxBriefRequestView.wxBriefAccountNameInfo.setOnClickListener(v-> displayInfoDialog(R.string.wxbrief_account_name_info));
        wxBriefRequestView.wxBriefOfficialBriefInfo.setOnClickListener(v-> displayInfoDialog(R.string.wxbrief_official_brief_info));

        return wxBriefRequestView.getRoot();
    }

    public void onViewCreated(View view, Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);

        viewModel.getMasterBriefingOptions().observe(getViewLifecycleOwner(), briefingOptions -> {
                    setTailoringOptionsSpinnerValues(briefingOptions);
                    setProductCodesSpinnerValues(briefingOptions);
                }
        );
        
        viewModel.getWxBriefUri().observe(getViewLifecycleOwner(), wxbriefUri -> {
            if (wxbriefUri == null) {
                return;
            }
            boolean canDisplayPdf = AppRepository.canDisplayPdf(getContext());
            if (canDisplayPdf) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(wxbriefUri, "application/pdf");
                startActivity(intent);
            } else {
                displayPdfNotDisplayable();
            }
        });
        
        viewModel.init();

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
        wxBriefRequestView = null;
    }


    private void setTailoringOptionsSpinnerValues(BriefingOptions briefingOptions) {
        wxBriefRequestView.wxBriefTailoringOptionsSpinner.setItems(briefingOptions.getTailoringOptionDescriptions()
                , briefingOptions.getSelectedTailoringOptions()
                , getString(R.string.select_1800wxbrief_tailoring_options)
                , selected -> viewModel.updateTailoringOptionsSelected(selected));
    }

    private void setProductCodesSpinnerValues(BriefingOptions briefingOptions) {
        wxBriefRequestView.wxBriefProductCodesSpinner.setItems(briefingOptions.getProductCodeDescriptionList()
                , briefingOptions.getProductCodesSelected()
                , getString(R.string.select_1800wxbrief_product_options)
                , selected -> viewModel.updateProductCodesSelected(selected));
    }

    private void displayPdfNotDisplayable() {
        displayInfoDialog( R.string.pdf_downloaded_but_no_pdf_viewer);
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
