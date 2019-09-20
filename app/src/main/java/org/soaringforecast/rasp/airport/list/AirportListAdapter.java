package org.soaringforecast.rasp.airport.list;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.databinding.AirportDetailBinding;
import org.soaringforecast.rasp.repository.Airport;
import org.soaringforecast.rasp.touchhelper.ItemTouchHelperAdapter;
import org.soaringforecast.rasp.touchhelper.ItemTouchHelperViewHolder;
import org.soaringforecast.rasp.touchhelper.OnStartDragListener;

import java.util.Collections;
import java.util.List;

// !!! Note that in order to get swipe to work needed to put views inside RelativeLayout rather than
// !!! just having them in ConstraintLayout
public class AirportListAdapter extends RecyclerView.Adapter<AirportListAdapter.BindingHolder>
        implements ItemTouchHelperAdapter {

    // TODO convert to use GenericRecyclerViewAdapter
    private List<Airport> airports;
    private AirportListAdapter.OnItemClickListener onItemClickListener;
    private OnStartDragListener dragStartListener;
    private NewAirportListListener newAirportListListener;

    public interface OnItemClickListener {
        void onItemClick(Airport airport);
    }

    public interface NewAirportListListener {
        void newAirportOrder(List<Airport> airports);
    }

    public AirportListAdapter() {
    }

    public AirportListAdapter setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        return this;
    }

    public AirportListAdapter setOnStartDragListener(OnStartDragListener dragStartListener) {
        this.dragStartListener = dragStartListener;
        return this;
    }

    public AirportListAdapter setNewAirportListListener(NewAirportListListener newAirportListListener){
        this.newAirportListListener = newAirportListListener;
        return this;
    }

    public void setAirportList(List<Airport> newAirportList) {
        this.airports = newAirportList;
        notifyDataSetChanged();
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AirportDetailBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent
                .getContext()), AirportListAdapter.BindingHolder.LAYOUT_RESOURCE, parent, false);
        return new AirportListAdapter.BindingHolder(binding);
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        holder.binding.setAirport(airports.get(position));

        if (dragStartListener != null) {
            // Start a drag whenever the handle view it touched
            holder.itemView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    dragStartListener.onStartDrag(holder);
                }
                return false;
            });
        }

        if (onItemClickListener != null) {
            // Not sure why holder.itemView.setOnClickListner didn't work
            holder.itemView.findViewById(R.id.airport_layout_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(holder.binding.getAirport());
                }
            });
        }
    }


    @Override
    public void onItemDismiss(int position) {
        airports.remove(position);
        notifyItemRemoved(position);
        notifyNewAirportList(airports);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(airports, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        notifyNewAirportList(airports);
        return true;
    }

    private void notifyNewAirportList(List<Airport> airports) {
        if (newAirportListListener != null){
            newAirportListListener.newAirportOrder(airports);
        }
    }


    @Override
    public int getItemCount() {
        return airports == null ? 0 : airports.size();
    }

    public static class BindingHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder {
        static final int LAYOUT_RESOURCE = R.layout.airport_detail;

        private final AirportDetailBinding binding;

        BindingHolder(AirportDetailBinding bindingView) {
            super(bindingView.getRoot());
            binding = bindingView;
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(itemView.getResources().getColor(R.color.drag_color));
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(itemView.getResources().getColor(R.color.drag_drop));
        }
    }
}

