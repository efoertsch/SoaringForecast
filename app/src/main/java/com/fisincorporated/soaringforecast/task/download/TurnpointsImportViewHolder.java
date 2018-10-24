package com.fisincorporated.soaringforecast.task.download;


import com.fisincorporated.soaringforecast.common.recycleradapter.GenericViewHolder;
import com.fisincorporated.soaringforecast.databinding.CupFileView;

import java.io.File;

public class TurnpointsImportViewHolder extends GenericViewHolder<File, CupFileView> {

    private CupFileView viewDataBinding;

    TurnpointsImportViewHolder(CupFileView bindingView) {
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
