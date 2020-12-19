package org.soaringforecast.rasp.one800wxbrief;

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
import org.soaringforecast.rasp.common.messages.CrashReport;
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

public class WxBriefRequestFragment extends DaggerFragment {

    private static final String TASKID = "TASKID";
    private WxBriefViewModel wxBriefViewModel;
    private WxBriefRequestView wxBriefRequestView;
    private long taskId;
    private int shortAnimationDuration;


    @Inject
    AppRepository appRepository;

    @Inject
    AppPreferences appPreferences;
    private boolean firstTime = true;


    public static WxBriefRequestFragment newInstance(long taskId) {
        WxBriefRequestFragment wxBriefRequestFragment = new WxBriefRequestFragment();
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

        // Retrieve and cache the system's default "short" animation time.
        shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        wxBriefRequestView = DataBindingUtil.inflate(inflater, R.layout.wx_brief_request_fragment, container, false);
        wxBriefRequestView.setLifecycleOwner(getViewLifecycleOwner()); // update UI based on livedata changes.
        wxBriefRequestView.setViewModel(wxBriefViewModel);
        wxBriefRequestView.wxBriefOfficialBriefInfo.setOnClickListener(v -> displayOfficialBriefingDialog());

        return wxBriefRequestView.getRoot();
    }

    public void onViewCreated(View view, Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);
        wxBriefViewModel.getMasterBriefingOptions().observe(getViewLifecycleOwner(), briefingOptions -> {
                    setTailoringOptionsSpinnerValues(briefingOptions);
                    setProductCodesSpinnerValues(briefingOptions);
                }
        );

        wxBriefViewModel.init();

        // Only using this observer so the validation logic in mediator will be fired
        wxBriefViewModel.getValidator().observe(getViewLifecycleOwner(), any -> {
        });

        wxBriefViewModel.getWxBriefUri().observe(getViewLifecycleOwner(), wxbriefUri -> {
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
    }

    private void setTailoringOptionsSpinnerValues(BriefingOptions briefingOptions) {
        wxBriefRequestView.wxBriefTailoringOptionsSpinner.setItems(briefingOptions.getTailoringOptionDescriptions()
                , briefingOptions.getSelectedTailoringOptions()
                , getString(R.string.select_wx800brief_tailoring_options)
                , selected -> wxBriefViewModel.updateTailoringOptionsSelected(selected));
    }

    private void setProductCodesSpinnerValues(BriefingOptions briefingOptions) {
        wxBriefRequestView.wxBriefProductCodesSpinner.setItems(briefingOptions.getProductCodeDescriptionList()
                , briefingOptions.getProductCodesSelected()
                , getString(R.string.select_wx800brief_product_options)
                , selected -> wxBriefViewModel.updateProductCodesSelected(selected));
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

    private void displayPdfNotDisplayable() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("1800WxBrief")
                .setMessage(getString(R.string.pdf_downloaded_but_no_pdf_viewer))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        alertDialog.setCanceledOnTouchOutside(false);
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

    private void crossfadeToSimpleBrief() {
        View simpleBriefLayout =  wxBriefRequestView.wxBriefSimpleBriefingLayout;
        View progressBarLayout = wxBriefRequestView.wxBriefFrameProgressBar;
        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        simpleBriefLayout.setAlpha(0f);
        simpleBriefLayout.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        simpleBriefLayout.animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration)
                .setListener(null);

    }

}
