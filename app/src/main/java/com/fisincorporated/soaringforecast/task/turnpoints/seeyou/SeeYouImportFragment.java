package com.fisincorporated.soaringforecast.task.turnpoints.seeyou;

import android.annotation.SuppressLint;
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
import com.fisincorporated.soaringforecast.common.recycleradapter.GenericRecyclerViewAdapter;
import com.fisincorporated.soaringforecast.messages.GoToDownloadImport;
import com.fisincorporated.soaringforecast.messages.ImportSeeYouFile;
import com.fisincorporated.soaringforecast.messages.SnackbarMessage;
import com.fisincorporated.soaringforecast.task.json.TurnpointFile;
import com.fisincorporated.soaringforecast.task.turnpoints.CommonTurnpointsImportFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;

public class SeeYouImportFragment extends CommonTurnpointsImportFragment<TurnpointFile, SeeYouImportViewHolder> {

    @Inject @Named("interceptor")
    OkHttpClient okHttpClient;

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
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected GenericRecyclerViewAdapter<TurnpointFile, SeeYouImportViewHolder> getRecyclerViewAdapter() {
        return new SeeYouImportRecyclerViewAdapter(null);
    }

    @Override
    public void onResume() {
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

    @SuppressLint("CheckResult")
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
                .setMessage(R.string.turnpoints_have_been_cleared)
                .setPositiveButton(R.string.ok, (dialog, id) -> {
                    // continue
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void doCustomImport() {
        EventBus.getDefault().post(new GoToDownloadImport());
    }

    @SuppressLint("CheckResult")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ImportSeeYouFile importSeeYouFile) {
        showProgressBar(true);
        TurnpointFile turnpointFile = importSeeYouFile.getTurnpointFile();
        Single<Integer> single = turnpointsImporterViewModel.setOkHttpClient(okHttpClient)
                .importTurnpointsFromUrl(turnpointFile.getRelativeUrl());
        single.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(numberTurnpoints -> {
                            showProgressBar(false);
                            post(new SnackbarMessage(getString(R.string.number_turnpoints_imported, numberTurnpoints)));
                        },
                        t -> {
                            showProgressBar(false);
                            post(new SnackbarMessage(getString(R.string.turnpoint_database_load_oops)));
                            // TODO mail crash
                        });
    }

}