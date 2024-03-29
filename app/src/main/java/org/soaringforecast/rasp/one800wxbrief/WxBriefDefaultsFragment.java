package org.soaringforecast.rasp.one800wxbrief;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
import org.soaringforecast.rasp.common.messages.PopThisFragmentFromBackStack;
import org.soaringforecast.rasp.databinding.WxBriefDefaultsView;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.WxBriefRequestResponse;
import org.soaringforecast.rasp.repository.AppRepository;

import javax.inject.Inject;

public class WxBriefDefaultsFragment extends MasterFragment {
    private WxBriefViewModel viewModel;
    private WxBriefDefaultsView wxBriefDefaultsView;

    @Inject
    AppRepository appRepository;

    @Inject
    AppPreferences appPreferences;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Note viewmodel is shared by activity
        viewModel = ViewModelProviders.of(requireActivity())
                .get(WxBriefViewModel.class)
                .setRepository(appRepository)
                .setAppPreferences(appPreferences);

        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        wxBriefDefaultsView = DataBindingUtil.inflate(inflater, R.layout.wx_brief_defaults, container, false);
        wxBriefDefaultsView.setLifecycleOwner(getViewLifecycleOwner()); // update UI based on livedata changes.
        wxBriefDefaultsView.setViewModel(viewModel);

        wxBriefDefaultsView.wxBriefCancel.setOnClickListener(v ->
                EventBus.getDefault().post(new PopThisFragmentFromBackStack()));

        wxBriefDefaultsView.wxBriefDefaultSaveBtn.setOnClickListener(v -> {
            viewModel.saveDefaultSettings();
            appPreferences.setFirstTimeforDefaultsDisplay(false);
            EventBus.getDefault().post(new PopThisFragmentFromBackStack());

        });

        return wxBriefDefaultsView.getRoot();
    }

    public void onViewCreated(View view, Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);
        viewModel.init(null);

    }

    public void onResume() {
        super.onResume();
        // Only using this observer so the validation logic in mediator will be fired
        viewModel.getValidator().observe(getViewLifecycleOwner(), any -> {
        });
        viewModel.startListening();
    }

    public void onPause(){
        super.onPause();
        // Only using this observer so the validation logic in mediator will be fired
        viewModel.getValidator().removeObservers(getViewLifecycleOwner());
        viewModel.stopListening();
    }

    public void onDestroyView(){
        super.onDestroyView();
        wxBriefDefaultsView = null;
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
