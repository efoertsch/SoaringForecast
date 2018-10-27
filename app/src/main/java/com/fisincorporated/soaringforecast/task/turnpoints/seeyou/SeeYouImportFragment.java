package com.fisincorporated.soaringforecast.task.turnpoints.seeyou;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.common.Constants;
import com.fisincorporated.soaringforecast.common.recycleradapter.GenericRecyclerViewAdapter;
import com.fisincorporated.soaringforecast.messages.GoToDownloadImport;
import com.fisincorporated.soaringforecast.messages.ImportSeeYouFile;
import com.fisincorporated.soaringforecast.messages.PopThisFragmentFromBackStack;
import com.fisincorporated.soaringforecast.messages.SnackbarMessage;
import com.fisincorporated.soaringforecast.task.json.TurnpointFile;
import com.fisincorporated.soaringforecast.task.turnpoints.CommonTurnpointsImportFragment;
import com.fisincorporated.soaringforecast.workmanager.TurnpointsImportWorker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.State;
import androidx.work.WorkManager;
import io.reactivex.disposables.CompositeDisposable;

public class SeeYouImportFragment extends CommonTurnpointsImportFragment<TurnpointFile, SeeYouImportViewHolder> {

    private CompositeDisposable disposable = new CompositeDisposable();

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
        setHasOptionsMenu(true);
        return super.onCreateView(inflater,container,savedInstanceState);
    }


     protected GenericRecyclerViewAdapter<TurnpointFile, SeeYouImportViewHolder> getRecyclerViewAdapter()  {
        return new SeeYouImportRecyclerViewAdapter(null);
    }


    @Override
    public void onResume(){
        super.onResume();
        turnpointsImporterViewModel.getTurnpointFiles().observe(this, turnpointFiles -> {
            recyclerViewAdapter.setItems(turnpointFiles, true);
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        turnpointsImporterViewModel.getTurnpointFiles().removeObservers(this);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.turnpoint_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.turnpoint_menu_clear_turnpoints:
                showClearTurnpointsDialog();
                return true;
            case R.id.turnpoint_menu_custom_import:
                doCustomImport();
                return true;
        }
        return false;
    }

    private void showClearTurnpointsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.turnpoints_no_hyphen)
                .setMessage(R.string.clearing_turnpoints_information)
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    clearTurnpointDatabase();
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {

                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void clearTurnpointDatabase() {
        showProgressBar(true);
        turnpointsImporterViewModel.clearTurnpointDatabase()
                .subscribe(numberDeleted -> {
            postNumberDeleted(numberDeleted);
                    showProgressBar(false);
         });

    }

    private void postNumberDeleted(Integer numberDeleted) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.turnpoints_no_hyphen)
                .setMessage(R.string.turnpoints_have_been_cleared);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void doCustomImport() {
        EventBus.getDefault().post(new GoToDownloadImport());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ImportSeeYouFile importSeeYouFile) {
        showProgressBar(true);
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
                        showProgressBar(false);
                        EventBus.getDefault().post(new SnackbarMessage(getString(R.string.import_successful, turnpointFile.getLocation())));
                        EventBus.getDefault().post(new PopThisFragmentFromBackStack());
                    } else {
                        if (workStatus != null && workStatus.getState().equals(State.FAILED)) {
                            showProgressBar(false);
                            EventBus.getDefault().post(new SnackbarMessage(getString(R.string.import_failed, turnpointFile.getLocation())));
                        }
                    }
                });

    }
}
