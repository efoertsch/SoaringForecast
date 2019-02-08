package org.soaringforecast.rasp.task.turnpoints.download;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.recycleradapter.GenericRecyclerViewAdapter;
import org.soaringforecast.rasp.messages.ImportFile;
import org.soaringforecast.rasp.messages.SnackbarMessage;
import org.soaringforecast.rasp.task.turnpoints.CommonTurnpointsImportFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TurnpointsDownloadFragment extends CommonTurnpointsImportFragment<File, TurnpointsDownloadViewHolder> {

    @Inject
    public TurnpointsDownloadFragment() {
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
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
    public void onPause() {
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


}