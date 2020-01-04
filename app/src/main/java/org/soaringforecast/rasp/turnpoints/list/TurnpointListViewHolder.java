package org.soaringforecast.rasp.turnpoints.list;

import org.soaringforecast.rasp.common.recycleradapter.GenericViewHolder;
import org.soaringforecast.rasp.databinding.TurnpointListView;
import org.soaringforecast.rasp.repository.Turnpoint;

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
