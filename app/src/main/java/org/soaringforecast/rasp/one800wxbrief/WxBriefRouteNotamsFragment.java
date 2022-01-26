package org.soaringforecast.rasp.one800wxbrief;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import org.soaringforecast.rasp.common.Constants;
import org.soaringforecast.rasp.common.MasterFragment;
import org.soaringforecast.rasp.common.messages.CrashReport;
import org.soaringforecast.rasp.databinding.WxBriefNotamsView;
import org.soaringforecast.rasp.one800wxbrief.messages.WXBriefDownloadsPermission;
import org.soaringforecast.rasp.one800wxbrief.messages.WxBriefUseAndDisclaimer;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.WxBriefRequestResponse;
import org.soaringforecast.rasp.repository.AppRepository;

import java.util.List;

import javax.inject.Inject;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class WxBriefRouteNotamsFragment extends WxBriefMasterFragment {

    private WxBriefNotamsView wxBriefView;

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
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        wxBriefView = DataBindingUtil.inflate(inflater, R.layout.wx_brief_route_notams, container, false);
        wxBriefView.setLifecycleOwner(getViewLifecycleOwner()); // update UI based on livedata changes.
        wxBriefView.setViewModel(viewModel);
        wxBriefView.wxBriefCancel.setOnClickListener(v -> getActivity().finish());
        wxBriefView.wxBriefNotamsAbbreviatedBriefInfo.setOnClickListener(v -> displayInfoDialog(R.string.wxbrief_notams_abbreviated_brief_info));
        wxBriefView.wxBriefAircraftRegistrationInfo.setOnClickListener(v -> displayInfoDialog(R.string.wxbrief_aircraft_registration_info));
        wxBriefView.wxBriefAccountNameInfo.setOnClickListener(v -> displayInfoDialog(R.string.wxbrief_account_name_info));

        return wxBriefView.getRoot();
    }

    public void onViewCreated(View view, Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);
        observeForPDFOption();
        viewModel.init(Constants.TypeOfBrief.NOTAMS);
    }

    public void onDestroyView() {
        super.onDestroyView();
        wxBriefView = null;
    }

}
