package com.fisincorporated.aviationweather.turnpoints.task;

import com.fisincorporated.aviationweather.common.recycleradapter.GenericViewHolder;
import com.fisincorporated.aviationweather.databinding.TurnpointSearchView;
import com.fisincorporated.aviationweather.repository.Turnpoint;

public class TurnpointSearchViewHolder extends GenericViewHolder<Turnpoint, TurnpointSearchView> {

    private TurnpointSearchView viewDataBinding;

    TurnpointSearchViewHolder(TurnpointSearchView bindingView) {
        super(bindingView);
        viewDataBinding = bindingView;
    }

    public void onBind(Turnpoint item, int position) {
        viewDataBinding.setTurnpoint(item);
        viewDataBinding.setPosition(position);
    }

    @Override
    public TurnpointSearchView getViewDataBinding() {
        return viewDataBinding;
    }

}
