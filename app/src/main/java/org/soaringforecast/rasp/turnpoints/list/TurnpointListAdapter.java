package org.soaringforecast.rasp.turnpoints.list;

import androidx.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.recycleradapter.GenericListClickListener;
import org.soaringforecast.rasp.common.recycleradapter.GenericRecyclerViewAdapter;
import org.soaringforecast.rasp.databinding.TurnpointListView;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.soaring.forecast.TurnpointBitmapUtils;

import java.util.List;

public class TurnpointListAdapter extends GenericRecyclerViewAdapter<Turnpoint, TurnpointListViewHolder> {

    private GenericListClickListener<Turnpoint> itemClickListener;
    private GenericListClickListener<Turnpoint> satelliteImageClickListener;
    private TurnpointBitmapUtils turnpointBitmapUtils;


    private TurnpointListAdapter(){
        super();
    }

    public static TurnpointListAdapter getInstance(){
        return new TurnpointListAdapter();
    }

    public TurnpointListAdapter setOnItemClickListener(GenericListClickListener<Turnpoint> turnpointGenericListClickListener ) {
        this.itemClickListener =  turnpointGenericListClickListener;
        return this;
    }

    public TurnpointListAdapter setSateliteOnItemClickListener(GenericListClickListener<Turnpoint> satelliteGenericListClickListener ) {
        this.satelliteImageClickListener =  satelliteGenericListClickListener;
        return this;
    }

    public TurnpointListAdapter setTurnpointBitmapUtils(TurnpointBitmapUtils turnpointBitmapUtils){
        this.turnpointBitmapUtils = turnpointBitmapUtils;
        return this;
    }


    public void setTurnpointList(List<Turnpoint> newTurnpointsList){
        if (newTurnpointsList == null){
           clear();
        } else {
            setItems(newTurnpointsList, true);
        }
    }

    @Override
    public TurnpointListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TurnpointListView binding = DataBindingUtil.inflate(LayoutInflater.from(parent
                .getContext()), R.layout.turnpoint_list_item_view, parent, false);
        return new TurnpointListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(TurnpointListViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.getViewDataBinding().setClickListener(itemClickListener);
        holder.getViewDataBinding().setSatelliteClickListener(satelliteImageClickListener);
        holder.getViewDataBinding().setTurnpointBitmapUtils(turnpointBitmapUtils);
    }

}
