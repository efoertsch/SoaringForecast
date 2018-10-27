package com.fisincorporated.soaringforecast.task.turnpoints.download;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.common.Constants;
import com.fisincorporated.soaringforecast.common.recycleradapter.GenericRecyclerViewAdapter;
import com.fisincorporated.soaringforecast.messages.ImportFile;
import com.fisincorporated.soaringforecast.messages.PopThisFragmentFromBackStack;
import com.fisincorporated.soaringforecast.messages.SnackbarMessage;
import com.fisincorporated.soaringforecast.task.turnpoints.CommonTurnpointsImportFragment;
import com.fisincorporated.soaringforecast.workmanager.TurnpointsImportWorker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import javax.inject.Inject;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.State;
import androidx.work.WorkManager;

public class TurnpointsDownloadFragment extends CommonTurnpointsImportFragment<File, TurnpointsDownloadViewHolder> {

    @Inject
    public TurnpointsDownloadFragment() {
    }

    public static TurnpointsDownloadFragment newInstance() {
        TurnpointsDownloadFragment turnpointsDownloadFragment = new TurnpointsDownloadFragment();
        return turnpointsDownloadFragment;
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater,container,savedInstanceState);
    }


    @Override
    protected GenericRecyclerViewAdapter<File, TurnpointsDownloadViewHolder> getRecyclerViewAdapter() {
        return new TurnpointsDownloadRecyclerViewAdapter(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        turnpointsImporterViewModel.getCupFiles().observe(this, files -> {
            if (files.size() == 0) {
                displayNoTurnpointFilesDialog();
            } else {
                recyclerViewAdapter.setItems(files, true);
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();
        turnpointsImporterViewModel.getCupFiles().removeObservers(this);
    }

    private void displayNoTurnpointFilesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.no_turnpoint_files_found)
                .setTitle(R.string.importable_files)
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    displayButFirstDialog();
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    exitThisFragment();
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
                    exitThisFragment();
                });
        builder.create().show();

    }

    private void startTurnpointBrowser() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://soaringweb.org/TP/NA.html#US"));
        startActivity(browserIntent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ImportFile importFile) {
        showProgressBar(true);
        Data.Builder builder = new Data.Builder();
        builder.putString(Constants.TURNPOINT_FILE_NAME, importFile.getFile().getName());
        OneTimeWorkRequest importTurnpointFileWorker =
                new OneTimeWorkRequest.Builder(TurnpointsImportWorker.class)
                        .setInputData(builder.build())
                        .build();
        WorkManager.getInstance().enqueue(importTurnpointFileWorker);
        WorkManager.getInstance().getStatusById(importTurnpointFileWorker.getId())
                .observe(this, workStatus -> {
                    // Do something with the status
                    if (workStatus != null && workStatus.getState().isFinished()) {
                        showProgressBar(false);
                        EventBus.getDefault().post(new SnackbarMessage(getString(R.string.import_successful, importFile.getFile().getName())));
                        EventBus.getDefault().post(new PopThisFragmentFromBackStack());
                    } else {
                        if (workStatus != null && workStatus.getState().equals(State.FAILED)){
                            showProgressBar(false);
                            EventBus.getDefault().post(new SnackbarMessage(getString(R.string.import_failed, importFile.getFile().getName())));
                        }
                    }
                });

    }
}
