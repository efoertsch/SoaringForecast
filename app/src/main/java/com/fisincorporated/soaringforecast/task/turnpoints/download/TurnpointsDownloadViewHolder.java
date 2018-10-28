package com.fisincorporated.soaringforecast.task.turnpoints.download;


import com.fisincorporated.soaringforecast.common.recycleradapter.GenericViewHolder;
import com.fisincorporated.soaringforecast.databinding.CupFileView;

import java.io.File;

public class TurnpointsDownloadViewHolder extends GenericViewHolder<File, CupFileView> {

    private CupFileView viewDataBinding;

    TurnpointsDownloadViewHolder(CupFileView bindingView) {
        super(bindingView);
        viewDataBinding = bindingView;
    }

    public void onBind(File item, int position) {
        viewDataBinding.setCupFile(item);
        viewDataBinding.setPosition(position);
    }

    @Override
    public CupFileView getViewDataBinding() {
        return viewDataBinding;
    }

}
