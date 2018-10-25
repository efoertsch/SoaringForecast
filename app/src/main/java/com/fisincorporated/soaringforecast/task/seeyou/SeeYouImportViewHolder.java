package com.fisincorporated.soaringforecast.task.seeyou;

import com.fisincorporated.soaringforecast.common.recycleradapter.GenericViewHolder;
import com.fisincorporated.soaringforecast.databinding.SeeYouFileView;
import com.fisincorporated.soaringforecast.task.json.TurnpointFile;

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

