package org.soaringforecast.rasp.turnpoints.search;

import org.soaringforecast.rasp.common.recycleradapter.GenericViewHolder;
import org.soaringforecast.rasp.databinding.TurnpointSearchView;
import org.soaringforecast.rasp.repository.Turnpoint;

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
