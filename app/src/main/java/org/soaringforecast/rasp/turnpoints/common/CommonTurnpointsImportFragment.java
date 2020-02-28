package org.soaringforecast.rasp.turnpoints.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.common.recycleradapter.GenericRecyclerViewAdapter;
import org.soaringforecast.rasp.common.recycleradapter.GenericViewHolder;
import org.soaringforecast.rasp.databinding.TurnpointsImportView;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.turnpoints.download.TurnpointsImporterViewModel;
import org.soaringforecast.rasp.utils.ViewUtilities;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
        setHasOptionsMenu(true);
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
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.import_turnpoints);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        compositeDisposable.dispose();
    }


    protected void showProgressBar(boolean setVisible){
        turnpointsImportView.turnpointImportsProgressBar.setVisibility(setVisible ? View.VISIBLE: View.GONE) ;
    }

    protected void post(Object object){
        EventBus.getDefault().post(object);
    }

}
