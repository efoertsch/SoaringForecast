package com.fisincorporated.soaringforecast.task.turnpoints;

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
import com.fisincorporated.soaringforecast.common.recycleradapter.GenericRecyclerViewAdapter;
import com.fisincorporated.soaringforecast.common.recycleradapter.GenericViewHolder;
import com.fisincorporated.soaringforecast.databinding.TurnpointsImportView;
import com.fisincorporated.soaringforecast.messages.PopThisFragmentFromBackStack;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.task.turnpoints.download.TurnpointsImporterViewModel;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;
import io.reactivex.disposables.CompositeDisposable;

public abstract class CommonTurnpointsImportFragment<T, VH extends GenericViewHolder> extends DaggerFragment {
    @Inject
    AppRepository appRepository;
    protected TurnpointsImportView turnpointsImportView;
    protected TurnpointsImporterViewModel turnpointsImporterViewModel;
    protected GenericRecyclerViewAdapter<T, VH > recyclerViewAdapter;
    protected CompositeDisposable compositeDisposable = new CompositeDisposable();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        turnpointsImporterViewModel = ViewModelProviders.of(this)
                .get(TurnpointsImporterViewModel.class)
                .setAppRepository(appRepository);
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
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext()
                , linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
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
