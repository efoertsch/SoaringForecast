package org.soaringforecast.rasp.turnpoints.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.recycleradapter.GenericListClickListener;
import org.soaringforecast.rasp.common.recycleradapter.GenericRecyclerViewAdapter;
import org.soaringforecast.rasp.common.recycleradapter.GenericViewHolder;
import org.soaringforecast.rasp.databinding.TurnpointListView;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.soaring.forecast.TurnpointBitmapUtils;

import java.util.List;

import androidx.databinding.DataBindingUtil;

public class TurnpointListAdapter extends GenericRecyclerViewAdapter<Turnpoint, TurnpointListAdapter.TurnpointListViewHolder> {

    private GenericListClickListener<Turnpoint> itemClickListener;
    private GenericListClickListener<Turnpoint> satelliteImageClickListener;
    private GenericListClickListener<Turnpoint> longClickListener;
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

    public TurnpointListAdapter setSatelliteOnItemClickListener(GenericListClickListener<Turnpoint> satelliteGenericListClickListener ) {
        this.satelliteImageClickListener =  satelliteGenericListClickListener;
        return this;
    }

    public TurnpointListAdapter setLongClickListener(GenericListClickListener<Turnpoint> longClickListener){
        this.longClickListener = longClickListener;
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
    public void onBindViewHolder(final TurnpointListViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
        if (itemClickListener != null) {
            holder.getViewDataBinding().setClickListener(itemClickListener);
        }
        if (satelliteImageClickListener != null) {
            holder.getViewDataBinding().setSatelliteClickListener(satelliteImageClickListener);
        }
        if (longClickListener != null){
            holder.getViewDataBinding().turnpointListLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    longClickListener.onItemClick(holder.viewDataBinding.getTurnpoint(), position);
                    return true;
                }
            });
        }

        holder.getViewDataBinding().setTurnpointBitmapUtils(turnpointBitmapUtils);

    }

    public class TurnpointListViewHolder extends GenericViewHolder<Turnpoint, TurnpointListView> {

        private TurnpointListView viewDataBinding;


        TurnpointListViewHolder(TurnpointListView bindingView) {
            super(bindingView);
            viewDataBinding = bindingView;
        }

        public void onBind(Turnpoint item, int position) {
            viewDataBinding.setTurnpoint(item);
            viewDataBinding.setPosition(position);
        }

        @Override
        public TurnpointListView getViewDataBinding() {
            return viewDataBinding;
        }

    }


}
