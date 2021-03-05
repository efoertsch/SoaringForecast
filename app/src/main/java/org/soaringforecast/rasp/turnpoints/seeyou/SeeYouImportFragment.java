package org.soaringforecast.rasp.turnpoints.seeyou;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.common.recycleradapter.GenericRecyclerViewAdapter;
import org.soaringforecast.rasp.retrofit.JSONServerApi;
import org.soaringforecast.rasp.turnpoints.common.CommonTurnpointsImportFragment;
import org.soaringforecast.rasp.turnpoints.json.TurnpointFile;
import org.soaringforecast.rasp.turnpoints.messages.GoToDownloadImport;
import org.soaringforecast.rasp.turnpoints.messages.ImportSeeYouFile;

import javax.inject.Inject;
import javax.inject.Named;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;

// TODO create viewmodel and move business logic to it
public class SeeYouImportFragment extends CommonTurnpointsImportFragment<TurnpointFile, SeeYouImportViewHolder> {

    @Inject @Named("no_interceptor")
    OkHttpClient okHttpClient;

    @Inject
    JSONServerApi JSONServerApi;

    @Inject
    public SeeYouImportFragment() {
    }

    public static SeeYouImportFragment newInstance() {
        return new SeeYouImportFragment();

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        turnpointsImporterViewModel.setGBSCJsonApi(JSONServerApi);

        setHasOptionsMenu(true);
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
        menu.clear();
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
                .setMessage(R.string.turnpoint_delete_all_really_sure)
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
        Disposable disposable = turnpointsImporterViewModel.clearTurnpointDatabase()
                .subscribe(numberDeleted -> {
                    postNumberDeleted(numberDeleted);
                    showProgressBar(false);
                });
        compositeDisposable.add(disposable);

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
        post(new GoToDownloadImport());
    }

    //TODO move logic into ViewModel
    @SuppressLint("CheckResult")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ImportSeeYouFile importSeeYouFile) {
        showProgressBar(true);
        TurnpointFile turnpointFile = importSeeYouFile.getTurnpointFile();
        Single<Integer> single = turnpointsImporterViewModel.setOkHttpClient(okHttpClient)
                .importTurnpointsFromUrl(turnpointFile.getRelativeUrl());
        Disposable disposable =  single.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(numberTurnpoints -> {
                            showProgressBar(false);
                            post(new SnackbarMessage(getString(R.string.number_turnpoints_imported, numberTurnpoints), Snackbar.LENGTH_LONG));
                        },
                        t -> {
                            showProgressBar(false);
                            post(new SnackbarMessage(getString(R.string.turnpoint_database_load_oops), Snackbar.LENGTH_INDEFINITE));
                            // TODO mail crash
                        });
        compositeDisposable.add(disposable);
    }

}
