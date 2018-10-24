package com.fisincorporated.soaringforecast.task.search;

import com.fisincorporated.soaringforecast.common.recycleradapter.GenericViewHolder;
import com.fisincorporated.soaringforecast.databinding.TurnpointSearchView;
import com.fisincorporated.soaringforecast.repository.Turnpoint;

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
