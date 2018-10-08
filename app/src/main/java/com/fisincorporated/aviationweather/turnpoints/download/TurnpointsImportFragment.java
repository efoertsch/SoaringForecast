package com.fisincorporated.aviationweather.turnpoints.download;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.common.Constants;
import com.fisincorporated.aviationweather.messages.ImportFile;
import com.fisincorporated.aviationweather.turnpoints.TurnpointProcessor;
import com.fisincorporated.aviationweather.workmanager.TurnpointsImportWorker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import dagger.android.support.DaggerFragment;

public class TurnpointsImportFragment extends DaggerFragment {

    @Inject
    TurnpointProcessor turnpointProcessor;

    @Inject
    TurnpointViewModel turnpointViewModel;

    private List<File> files = new ArrayList<>();

    private TurnpointsImportRecyclerViewAdapter recyclerViewAdapter;

    @Inject
    public TurnpointsImportFragment() {
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.turnpoint_import_files, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.turnpoint_imports_recycler_view);
        recyclerViewAdapter = new TurnpointsImportRecyclerViewAdapter(files);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext()
                , linearLayoutManager.getOrientation());

        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setAdapter(recyclerViewAdapter);

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        //TODO refresh file list
        files = turnpointProcessor.getCupFileList();
        if (files.size() == 0) {
            displayNoTurnpointFilesDialog();
        } else {
            recyclerViewAdapter.updateFileList(files);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void displayNoTurnpointFilesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.no_turnpoint_files_found)
                .setTitle(R.string.no_cup_files_found)
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    dialog.dismiss();
                    displayButFirstDialog();
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    dialog.dismiss();
                    cancelActivity();
                });
        builder.create().show();
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
                    cancelActivity();
                });
        builder.create().show();

    }

    private void startTurnpointBrowser() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://soaringweb.org/TP/NA.html#US"));
        startActivity(browserIntent);
    }

    private void cancelActivity() {
        getActivity().finish();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ImportFile importFile) {
        //TODO - set up Work Observer and display(?) snackbar when import done?
        Data.Builder builder = new Data.Builder();
        builder.putString(Constants.TURNPOINT_FILE_NAME, importFile.getFile().getName());

        OneTimeWorkRequest importTurnpointFileWorker =
                new OneTimeWorkRequest.Builder(TurnpointsImportWorker.class)
                        .setInputData(builder.build())
                        .build();
        WorkManager.getInstance().enqueue(importTurnpointFileWorker);
    }
}
