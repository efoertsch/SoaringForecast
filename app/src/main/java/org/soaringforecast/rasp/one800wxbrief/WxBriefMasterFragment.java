package org.soaringforecast.rasp.one800wxbrief;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.common.MasterFragment;
import org.soaringforecast.rasp.common.messages.CrashReport;
import org.soaringforecast.rasp.databinding.WxBriefRequestView;
import org.soaringforecast.rasp.one800wxbrief.messages.WxBriefUseAndDisclaimer;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.WxBriefRequestResponse;
import org.soaringforecast.rasp.repository.AppRepository;

import java.util.List;

import javax.inject.Inject;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class WxBriefMasterFragment extends MasterFragment implements EasyPermissions.PermissionCallbacks  {
    protected static final String TASKID = "TASKID";
    protected static final int WRITE_DOWNLOADS_ACCESS = 7777;
    protected WxBriefViewModel viewModel;
    protected long taskId = -1;

    @Inject
    AppRepository appRepository;

    @Inject
    AppPreferences appPreferences;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTaskId();
        // Note viewmodel is shared by activity
        viewModel = ViewModelProviders.of(requireActivity())
                .get(WxBriefViewModel.class)
                .setRepository(appRepository)
                .setAppPreferences(appPreferences)
                .setTaskId(taskId);


        setHasOptionsMenu(true);
    }

    protected void getTaskId() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            taskId = bundle.getLong(TASKID, -1);
        }
    }

    public void onResume() {
        super.onResume();
        // Only using this observer so the validation logic in mediator will be fired
        viewModel.getValidator().observe(getViewLifecycleOwner(), any -> {
        });
        viewModel.startListening();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.wxbrief_use_and_disclaimer_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.wxbrief_use_and_disclaimer_menu:
                viewModel.showWxBriefDisclaimer();
                EventBus.getDefault().post(new WxBriefUseAndDisclaimer());
                return true;
            default:
                break;
        }

        return false;
    }

    public void onPause() {
        super.onPause();
        // Only using this observer so the validation logic in mediator will be fired
        viewModel.getValidator().removeObservers(getViewLifecycleOwner());
        viewModel.stopListening();
    }

    protected void observeForPDFOption() {
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

    private void displayPdfNotDisplayable() {
        displayInfoDialog(R.string.pdf_downloaded_but_no_pdf_viewer);
    }


    protected void displayInfoDialog(@StringRes int stringResourceId) {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setView(R.layout.wx_brief_info_layout)
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
