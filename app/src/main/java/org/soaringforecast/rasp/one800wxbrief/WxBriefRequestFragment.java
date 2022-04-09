package org.soaringforecast.rasp.one800wxbrief;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.messages.CrashReport;
import org.soaringforecast.rasp.databinding.WxBriefRequestView;
import org.soaringforecast.rasp.one800wxbrief.messages.WxBriefUseAndDisclaimer;
import org.soaringforecast.rasp.one800wxbrief.options.BriefingOptions;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.WxBriefRequestResponse;
import org.soaringforecast.rasp.repository.AppRepository;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class WxBriefRequestFragment extends WxBriefMasterFragment{

    private WxBriefRequestView wxBriefView;

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
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        wxBriefView = DataBindingUtil.inflate(inflater, R.layout.wx_brief_request_fragment, container, false);
        wxBriefView.setLifecycleOwner(getViewLifecycleOwner()); // update UI based on livedata changes.
        wxBriefView.setViewModel(viewModel);
        wxBriefView.wxBriefCancel.setOnClickListener(v -> getActivity().finish());

        wxBriefView.wxBriefAircraftRegistrationInfo.setOnClickListener(v -> displayInfoDialog(R.string.wxbrief_aircraft_registration_info));
        wxBriefView.wxBriefAccountNameInfo.setOnClickListener(v -> displayInfoDialog(R.string.wxbrief_account_name_info));
        //wxBriefView.wxBriefOfficialBriefInfo.setOnClickListener(v -> displayInfoDialog(R.string.wxbrief_official_brief_info));
        wxBriefView.wxBriefDepartureDateInfo.setOnClickListener(v -> displayInfoDialog(R.string.wxbrief_departure_date_info));
        wxBriefView.wxBriefSimpleBriefingText.setMovementMethod(new ScrollingMovementMethod());
        return wxBriefView.getRoot();
    }

    public void onViewCreated(View view, Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);

        viewModel.getMasterBriefingOptions().observe(getViewLifecycleOwner(), briefingOptions -> {
                    setTailoringOptionsSpinnerValues(briefingOptions);
                    setProductCodesSpinnerValues(briefingOptions);
                }
        );
        viewModel.init(null);
        observeForPDFOption();
    }

    public void onDestroyView() {
        super.onDestroyView();
        wxBriefView = null;
    }

    private void setTailoringOptionsSpinnerValues(BriefingOptions briefingOptions) {
        wxBriefView.wxBriefTailoringOptionsSpinner.setItems(briefingOptions.getTailoringOptionDescriptions()
                , briefingOptions.getSelectedTailoringOptions()
                , getString(R.string.select_1800wxbrief_tailoring_options)
                , selected -> viewModel.updateTailoringOptionsSelected(selected));
    }

    private void setProductCodesSpinnerValues(BriefingOptions briefingOptions) {
        wxBriefView.wxBriefProductCodesSpinner.setItems(briefingOptions.getProductCodeDescriptionList()
                , briefingOptions.getProductCodesSelected()
                , getString(R.string.select_1800wxbrief_product_options)
                , selected -> viewModel.updateProductCodesSelected(selected));
    }

}
