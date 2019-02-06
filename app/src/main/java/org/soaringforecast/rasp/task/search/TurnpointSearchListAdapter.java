package org.soaringforecast.rasp.task.search;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.recycleradapter.GenericListClickListener;
import org.soaringforecast.rasp.common.recycleradapter.GenericRecyclerViewAdapter;
import org.soaringforecast.rasp.databinding.TurnpointSearchView;
import org.soaringforecast.rasp.repository.Turnpoint;

import java.util.ArrayList;
import java.util.List;

public class TurnpointSearchListAdapter extends GenericRecyclerViewAdapter<Turnpoint, TurnpointSearchViewHolder> {

    private List<Turnpoint> turnpoints = new ArrayList<>();
    private GenericListClickListener<Turnpoint> itemClickListener;
    private NewTurnpointListListener newTurnpointListListener;

    public interface NewTurnpointListListener {
        void newTurnpointOrder(List<Turnpoint> turnpoints);
    }

    public TurnpointSearchListAdapter(){
        super();
    }

    public void setOnItemClickListener(GenericListClickListener<Turnpoint> turnpointGenericListClickListener ) {
        this.itemClickListener =  turnpointGenericListClickListener;
    }


    public void setTurnpointList(List<Turnpoint> newTurnpointsList){
        if (newTurnpointsList == null){
           clear();
        } else {
            setItems(newTurnpointsList, true);
        }
    }

    @Override
    public TurnpointSearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TurnpointSearchView binding = DataBindingUtil.inflate(LayoutInflater.from(parent
                .getContext()), R.layout.turnpoint_search_item_view, parent, false);
        return new TurnpointSearchViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(TurnpointSearchViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.getViewDataBinding().setClickListener(itemClickListener);
    }

}
