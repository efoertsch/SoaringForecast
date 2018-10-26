package com.fisincorporated.soaringforecast.task.seeyou;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.common.Constants;
import com.fisincorporated.soaringforecast.databinding.TurnpointsImportView;
import com.fisincorporated.soaringforecast.messages.ImportSeeYouFile;
import com.fisincorporated.soaringforecast.messages.PopThisFragmentFromBackStack;
import com.fisincorporated.soaringforecast.messages.SnackbarMessage;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.task.download.TurnpointsImporterViewModel;
import com.fisincorporated.soaringforecast.task.json.TurnpointFile;
import com.fisincorporated.soaringforecast.workmanager.TurnpointsImportWorker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.State;
import androidx.work.WorkManager;
import dagger.android.support.DaggerFragment;

public class SeeYouImportFragment extends DaggerFragment {

    @Inject
    AppRepository appRepository;

    TurnpointsImportView turnpointsImportView;

    TurnpointsImporterViewModel turnpointsImporterViewModel;

    private SeeYouImportRecyclerViewAdapter recyclerViewAdapter;

    @Inject
    public SeeYouImportFragment() {
    }

    public static SeeYouImportFragment newInstance() {
        SeeYouImportFragment seeYouImportFragment = new SeeYouImportFragment();
        return seeYouImportFragment;
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        turnpointsImportView = DataBindingUtil.inflate(inflater, R.layout.turnpoint_import_files, container, false);

        turnpointsImporterViewModel = ViewModelProviders.of(this)
                .get(TurnpointsImporterViewModel.class)
                .setAppRepository(appRepository);

        RecyclerView recyclerView = turnpointsImportView.turnpointImportsRecyclerView;
        recyclerViewAdapter = new SeeYouImportRecyclerViewAdapter(null);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext()
                , linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(recyclerViewAdapter);
        turnpointsImporterViewModel.getTurnpointFiles().observe(this, turnpointFiles -> {
            recyclerViewAdapter.setItems(turnpointFiles, true);
        });

        return turnpointsImportView.getRoot();
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.import_turnpoints);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    private void exitThisFragment() {
        EventBus.getDefault().post(new PopThisFragmentFromBackStack());
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ImportSeeYouFile importSeeYouFile) {
        turnpointsImportView.turnpointImportsProgressBar.setVisibility(View.VISIBLE);
        TurnpointFile turnpointFile = importSeeYouFile.getTurnpointFile();
        Data.Builder builder = new Data.Builder();
        builder.putString(Constants.TURNPOINT_FILE_URL, turnpointFile.getLocation() + "/" + turnpointFile.getFilename());
        OneTimeWorkRequest importTurnpointFileWorker =
                new OneTimeWorkRequest.Builder(TurnpointsImportWorker.class)
                        .setInputData(builder.build())
                        .build();
        WorkManager.getInstance().enqueue(importTurnpointFileWorker);
        WorkManager.getInstance().getStatusById(importTurnpointFileWorker.getId())
                .observe(this, workStatus -> {
                    // Do something with the status
                    if (workStatus != null && workStatus.getState().isFinished() && workStatus.getState().equals(State.SUCCEEDED)) {
                        turnpointsImportView.turnpointImportsProgressBar.setVisibility(View.GONE);
                        EventBus.getDefault().post(new SnackbarMessage(getString(R.string.import_successful, turnpointFile.getLocation())));
                        EventBus.getDefault().post(new PopThisFragmentFromBackStack());
                    } else {
                        if (workStatus != null && workStatus.getState().equals(State.FAILED)) {
                            turnpointsImportView.turnpointImportsProgressBar.setVisibility(View.GONE);
                            EventBus.getDefault().post(new SnackbarMessage(getString(R.string.import_failed, turnpointFile.getLocation())));
                        }
                    }
                });

    }
}
