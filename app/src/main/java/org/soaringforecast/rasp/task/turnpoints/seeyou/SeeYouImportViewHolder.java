package org.soaringforecast.rasp.task.turnpoints.seeyou;

import org.soaringforecast.rasp.common.recycleradapter.GenericViewHolder;
import org.soaringforecast.rasp.databinding.SeeYouFileView;
import org.soaringforecast.rasp.task.json.TurnpointFile;

public class SeeYouImportViewHolder extends GenericViewHolder<TurnpointFile, SeeYouFileView> {

    private SeeYouFileView viewDataBinding;

    SeeYouImportViewHolder(SeeYouFileView bindingView) {
        super(bindingView);
        viewDataBinding = bindingView;
    }

    public void onBind(TurnpointFile item, int position) {
        viewDataBinding.setTurnpointFile(item);
        viewDataBinding.setPosition(position);
    }

    @Override
    public SeeYouFileView getViewDataBinding() {
        return viewDataBinding;
    }

}

