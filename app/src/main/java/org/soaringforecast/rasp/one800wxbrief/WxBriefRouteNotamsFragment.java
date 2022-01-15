package org.soaringforecast.rasp.one800wxbrief;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import org.soaringforecast.rasp.common.Constants;
import org.soaringforecast.rasp.common.MasterFragment;
import org.soaringforecast.rasp.common.messages.CrashReport;
import org.soaringforecast.rasp.common.messages.PopThisFragmentFromBackStack;
import org.soaringforecast.rasp.databinding.WxBriefNotamsView;
import org.soaringforecast.rasp.one800wxbrief.messages.WXBriefDownloadsPermission;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.WxBriefRequestResponse;
import org.soaringforecast.rasp.repository.AppRepository;

import java.util.List;

import javax.inject.Inject;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class WxBriefRouteNotamsFragment extends MasterFragment implements EasyPermissions.PermissionCallbacks {
    private static final String TASKID = "TASKID";
    private static final int WRITE_DOWNLOADS_ACCESS = 54321;
    private static final int READ_DOWNLOADS_ACCESS = 2222;
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
        wxBriefNotamsView.wxBriefNotamsAbbreviatedBriefInfo.setOnClickListener(v -> displayInfoDialog(R.string.wxbrief_notams_abbreviated_brief_info));
        wxBriefNotamsView.wxBriefAircraftRegistrationInfo.setOnClickListener(v -> displayInfoDialog(R.string.wxbrief_aircraft_registration_info));
        wxBriefNotamsView.wxBriefAccountNameInfo.setOnClickListener(v -> displayInfoDialog(R.string.wxbrief_account_name_info));

        return wxBriefNotamsView.getRoot();
    }

    public void onViewCreated(View view, Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);
        viewModel.init(Constants.TypeOfBrief.NOTAMS);

        viewModel.getWxBriefUri().observe(getViewLifecycleOwner(), wxbriefUri -> {
            if (wxbriefUri == Uri.EMPTY) {
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

    public void onResume() {
        super.onResume();
        // Only using this observer so the validation logic in mediator will be fired
        viewModel.getValidator().observe(getViewLifecycleOwner(), any -> {
        });
        viewModel.startListening();

    }

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


    private void displayPdfNotDisplayable() {
        displayInfoDialog(R.string.pdf_downloaded_but_no_pdf_viewer);
    }

    private void displayInfoDialog(@StringRes int stringResourceId) {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setView(R.layout.wx_brief_info_layout)
                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        TextView title = alertDialog.findViewById(R.id.wxbrief_info_title);
        if (title != null) {
            title.setText(R.string.wxbrief_title);
        }
        TextView info = alertDialog.findViewById(R.id.wxbrief_info);
        if (info != null) {
            info.setText(getText(stringResourceId));
        }

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

    @Subscribe(threadMode = ThreadMode.MAIN)

    public void onMessageEvent(WXBriefDownloadsPermission wxBriefDownloadsPermission) {
        if (EasyPermissions.hasPermissions(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // continue
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rational_pdf_downloads_dir), WRITE_DOWNLOADS_ACCESS, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Timber.d("Permission has been granted");
        if (requestCode == WRITE_DOWNLOADS_ACCESS && perms != null & perms.size() >= 1 && perms.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // carry on
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (requestCode == WRITE_DOWNLOADS_ACCESS && perms != null & perms.size() >= 1 && perms.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            viewModel.resetSelectedBriefFormatPosition();
            Timber.d("Permission has been denied");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.no_permission_to_write_downloads_dir)
                    .setTitle(R.string.permission_denied)
                    .setPositiveButton(R.string.ok, (dialog, id) -> {
                        viewModel.resetSelectedBriefFormatPosition();
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }


}
