package com.fisincorporated.aviationweather.turnpoints.task;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.common.recycleradapter.GenericListClickListener;
import com.fisincorporated.aviationweather.common.recycleradapter.GenericRecyclerViewAdapter;
import com.fisincorporated.aviationweather.databinding.TurnpointSearchView;
import com.fisincorporated.aviationweather.repository.Turnpoint;
import com.fisincorporated.aviationweather.touchhelper.ItemTouchHelperAdapter;
import com.fisincorporated.aviationweather.touchhelper.OnStartDragListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TurnpointSearchListAdapter extends GenericRecyclerViewAdapter<Turnpoint, TurnpointSearchViewHolder> 
        implements ItemTouchHelperAdapter{

    private List<Turnpoint> turnpoints = new ArrayList<>();
    private GenericListClickListener<Turnpoint> itemClickListener;
    private OnStartDragListener dragStartListener;
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

    public TurnpointSearchListAdapter setOnStartDragListener(OnStartDragListener dragStartListener) {
        this.dragStartListener = dragStartListener;
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
    public TurnpointSearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TurnpointSearchView binding = DataBindingUtil.inflate(LayoutInflater.from(parent
                .getContext()), R.layout.turnpoint_search_item_view, parent, false);
        return new TurnpointSearchViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(TurnpointSearchViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.getViewDataBinding().setClickListener(itemClickListener);

        if (dragStartListener != null) {
            // Start a drag whenever the handle view it touched
            holder.itemView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    dragStartListener.onStartDrag(holder);
                }
                return false;
            });
        }

//        if (itemClickListener != null) {
//            // Not sure why holder.itemView.setOnClickListner didn't work
//            holder.itemView.findViewById(R.id.turnpoint_layout).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    itemClickListener.onItemClick(holder.getViewDataBinding().getTurnpoint());
//                }
//            });
//        }
    }

    @Override
    public void onItemDismiss(int position) {
        turnpoints.remove(position);
        notifyItemRemoved(position);
        notifyNewTurnpointsList(turnpoints);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(turnpoints, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        notifyNewTurnpointsList(turnpoints);
        return true;
    }

    private void notifyNewTurnpointsList(List<Turnpoint> turnpoints) {
        if (newTurnpointListListener != null){
           newTurnpointListListener.newTurnpointOrder(turnpoints);
        }
    }

    
 

}
