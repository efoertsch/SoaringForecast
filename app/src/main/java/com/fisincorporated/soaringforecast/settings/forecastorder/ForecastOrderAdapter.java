package com.fisincorporated.soaringforecast.settings.forecastorder;

import android.annotation.SuppressLint;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.common.recycleradapter.GenericRecyclerViewAdapter;
import com.fisincorporated.soaringforecast.common.recycleradapter.GenericViewHolder;
import com.fisincorporated.soaringforecast.databinding.ForecastOrderView;
import com.fisincorporated.soaringforecast.soaring.json.Forecast;
import com.fisincorporated.soaringforecast.touchhelper.ItemTouchHelperAdapter;
import com.fisincorporated.soaringforecast.touchhelper.ItemTouchHelperViewHolder;
import com.fisincorporated.soaringforecast.utils.BitmapImageUtils;

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
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }

    }
}


