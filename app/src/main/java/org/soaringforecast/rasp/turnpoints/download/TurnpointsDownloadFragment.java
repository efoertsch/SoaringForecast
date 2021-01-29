package org.soaringforecast.rasp.turnpoints.download;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.messages.PopThisFragmentFromBackStack;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.common.recycleradapter.GenericRecyclerViewAdapter;
import org.soaringforecast.rasp.turnpoints.common.CommonTurnpointsImportFragment;
import org.soaringforecast.rasp.turnpoints.messages.ImportFile;
import org.soaringforecast.rasp.turnpoints.messages.SendEmail;
import org.soaringforecast.rasp.turnpoints.messages.UnknownSeeYouFormat;

import java.io.File;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class TurnpointsDownloadFragment extends CommonTurnpointsImportFragment<File, TurnpointsDownloadViewHolder>
        implements EasyPermissions.PermissionCallbacks {

    private static final int READ_DOWNLOADS_ACCESS = 2222;
    private boolean bypassOnResume = false;


    public static TurnpointsDownloadFragment newInstance() {
        return new TurnpointsDownloadFragment();

    }
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        // Check for permission to read downloads directory
        if (EasyPermissions.hasPermissions(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            getTurnpoints();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rational_read_downloads_dir), READ_DOWNLOADS_ACCESS, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        return view;
    }

    @Override
    protected GenericRecyclerViewAdapter<File, TurnpointsDownloadViewHolder> getRecyclerViewAdapter() {
        return new TurnpointsDownloadRecyclerViewAdapter(null);
    }

    private void getTurnpoints() {
        // have permission so check for downloaded files
        turnpointsImporterViewModel.getCupFiles().observe(getViewLifecycleOwner(), files -> {
            if (files == null || files.size() == 0) {
                displayNoTurnpointFilesDialog();
            } else {
                recyclerViewAdapter.setItems(files, true);
            }
        });
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
        if (requestCode == READ_DOWNLOADS_ACCESS && perms != null & perms.size() >= 1 && perms.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            getTurnpoints();
        }
    }


    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (requestCode == READ_DOWNLOADS_ACCESS && perms != null & perms.size() >= 1 && perms.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Timber.d("Permission has been denied");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.no_permission_to_read_downloads_dir)
                    .setTitle(R.string.permission_denied)
                    .setPositiveButton(R.string.ok, (dialog, id) -> {
                        EventBus.getDefault().post(new PopThisFragmentFromBackStack());
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
            bypassOnResume = true;
        }
    }

    private void displayNoTurnpointFilesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.no_turnpoint_files_found)
                .setTitle(R.string.importable_files)
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    displayButFirstDialog();
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    post(new PopThisFragmentFromBackStack());
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void displayButFirstDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.need_to_download_cup_files)
                .setTitle(R.string.but_first)
                .setPositiveButton(R.string.start_browser, (dialog, id) -> {
                    dialog.dismiss();
                    startTurnpointBrowser();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                    post(new PopThisFragmentFromBackStack());
                });
        builder.create().show();

    }

    private void startTurnpointBrowser() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://soaringweb.org/TP/NA.html#US"));
        startActivity(browserIntent);
    }

    //TODO move logic into ViewModel
    @SuppressLint("CheckResult")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ImportFile importFile) {
        showProgressBar(true);
        Single<Integer> single = turnpointsImporterViewModel.importTurnpointFileFromDownloadDirectory(importFile.getFile().getName());
        Disposable disposable = single.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(numberTurnpoints -> {
                            showProgressBar(false);
                            post(new SnackbarMessage(getString(R.string.number_turnpoints_imported, numberTurnpoints), Snackbar.LENGTH_LONG));
                        },
                        t -> {
                            showProgressBar(false);
                            post(new SnackbarMessage(getString(R.string.turnpoint_database_load_oops), Snackbar.LENGTH_INDEFINITE));
                            //TODO mail crash
                        });
        compositeDisposable.add(disposable);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UnknownSeeYouFormat unknownSeeYouFormat) {
        showProgressBar(false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.invalid_seeyou_file_format)
                .setMessage(getString(R.string.unknown_seeyou_file_format, unknownSeeYouFormat.getSeeYouFormat(), getString(R.string.send_email)))
                .setPositiveButton(R.string.ok, (dialog, id) -> {
                    // just return
                })
                .setNegativeButton(R.string.send_email, (dialog, which) -> {
                     sendEmail(unknownSeeYouFormat);
                });
        builder.create().show();

    }

    private void sendEmail(UnknownSeeYouFormat unknownSeeYouFormat) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_EMAIL, getString(R.string.One800WXBriefID));
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.invalid_seeyou_file_format));
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.invalid_seeyou_file_email_text, unknownSeeYouFormat.getSeeYouFormat()));
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        } catch (Exception e) {
            post(new SnackbarMessage(getString(R.string.error_in_emailing_invalid_seeyou_format)));
        }
    }

}
