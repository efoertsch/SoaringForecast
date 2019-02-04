package org.soaringforecast.rasp.settings.forecastorder;

import android.annotation.SuppressLint;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.recycleradapter.GenericRecyclerViewAdapter;
import org.soaringforecast.rasp.common.recycleradapter.GenericViewHolder;
import org.soaringforecast.rasp.databinding.ForecastOrderView;
import org.soaringforecast.rasp.soaring.json.Forecast;
import org.soaringforecast.rasp.touchhelper.ItemTouchHelperAdapter;
import org.soaringforecast.rasp.touchhelper.ItemTouchHelperViewHolder;
import org.soaringforecast.rasp.utils.BitmapImageUtils;

import java.util.Collections;
import java.util.List;

public class ForecastOrderAdapter extends GenericRecyclerViewAdapter<Forecast, ForecastOrderAdapter.ForecastOrderViewHolder>
        implements ItemTouchHelperAdapter {

    private ForecastOrderAdapter.OnItemClickListener onItemClickListener;
    private NewForecastOrderListener newForecastListListener;

    public interface OnItemClickListener {
        void onItemClick(Forecast forecast);
    }

    public interface NewForecastOrderListener {
        void newForecastOrder(List<Forecast> forecasts);
    }

    public ForecastOrderAdapter() {
    }

    public void setNewForecastOrderListener(NewForecastOrderListener newForecastListListener){
        this.newForecastListListener = newForecastListListener;
    }

    public ForecastOrderAdapter setOnItemClickListener(ForecastOrderAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        return this;
    }
    
    public void setForecastList(List<Forecast> newForecastList) {
        setItems(newForecastList);
        notifyDataSetChanged();
    }

    @Override
    public ForecastOrderAdapter.ForecastOrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ForecastOrderView binding = DataBindingUtil.inflate
                (LayoutInflater.from(parent.getContext())
                        , ForecastOrderAdapter.ForecastOrderViewHolder.LAYOUT_RESOURCE, parent, false);
        return new ForecastOrderAdapter.ForecastOrderViewHolder(binding);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(ForecastOrderAdapter.ForecastOrderViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (onItemClickListener != null) {
            // Not sure why holder.itemView.setOnClickListner didn't work
            holder.getViewDataBinding().soaringForecastInfo.setOnClickListener(v ->
                    onItemClickListener.onItemClick(holder.getViewDataBinding().getForecast()));
        }
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(getItems(), fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        notifyNewForecastOrder();
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        // No swipe to dismiss
    }

    private void notifyNewForecastOrder() {
        if (newForecastListListener != null) {
            newForecastListListener.newForecastOrder(getItems());
        }
    }


    public static class ForecastOrderViewHolder extends GenericViewHolder<Forecast, ForecastOrderView>
        implements ItemTouchHelperViewHolder{

        protected static final int LAYOUT_RESOURCE = R.layout.forecast_order_row;

        private ForecastOrderView viewDataBinding;

        public ForecastOrderViewHolder(ForecastOrderView bindingView) {
            super(bindingView);
            viewDataBinding = bindingView;
        }

        public void onBind(Forecast forecast, int position) {
            viewDataBinding.setForecast(forecast);
            BitmapImageUtils.getForecastDrawable( forecast.getForecastCategory(),viewDataBinding.soaringForecastInfo);
        }

        @Override
        public ForecastOrderView getViewDataBinding() {
            return viewDataBinding;
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


