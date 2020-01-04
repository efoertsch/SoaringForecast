package org.soaringforecast.rasp.turnpoints.common;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.common.recycleradapter.GenericRecyclerViewAdapter;
import org.soaringforecast.rasp.common.recycleradapter.GenericViewHolder;
import org.soaringforecast.rasp.databinding.TurnpointsImportView;
import org.soaringforecast.rasp.common.messages.PopThisFragmentFromBackStack;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.turnpoints.download.TurnpointsImporterViewModel;
import org.soaringforecast.rasp.utils.ViewUtilities;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;
import io.reactivex.disposables.CompositeDisposable;

public abstract class CommonTurnpointsImportFragment<T, VH extends GenericViewHolder> extends DaggerFragment {

    @Inject
    AppRepository appRepository;

    @Inject
    AppPreferences appPreferences;

    private TurnpointsImportView turnpointsImportView;
    protected TurnpointsImporterViewModel turnpointsImporterViewModel;
    protected GenericRecyclerViewAdapter<T, VH > recyclerViewAdapter;
    protected CompositeDisposable compositeDisposable = new CompositeDisposable();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        turnpointsImporterViewModel = ViewModelProviders.of(this)
                .get(TurnpointsImporterViewModel.class)
                .setAppRepository(appRepository)
                .setAppPreferences(appPreferences);
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        turnpointsImportView = DataBindingUtil.inflate(inflater, R.layout.turnpoint_import_files, container, false);

        RecyclerView recyclerView = turnpointsImportView.turnpointImportsRecyclerView;

        recyclerViewAdapter = getRecyclerViewAdapter();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        ViewUtilities.addRecyclerViewDivider(getContext(), linearLayoutManager.getOrientation(), recyclerView);
        recyclerView.setAdapter(recyclerViewAdapter);
        return turnpointsImportView.getRoot();

    }

   protected abstract GenericRecyclerViewAdapter getRecyclerViewAdapter();

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

    @Override
    public void onDestroy(){
        super.onDestroy();
        compositeDisposable.dispose();
    }


    protected void exitThisFragment() {
        EventBus.getDefault().post(new PopThisFragmentFromBackStack());
    }

    protected void showProgressBar(boolean setVisible){
        turnpointsImportView.turnpointImportsProgressBar.setVisibility(setVisible ? View.VISIBLE: View.GONE) ;
    }

    protected void post(Object object){
        EventBus.getDefault().post(object);
    }

}
