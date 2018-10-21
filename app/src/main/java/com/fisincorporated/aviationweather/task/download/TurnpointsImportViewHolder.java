package com.fisincorporated.aviationweather.task.download;


import com.fisincorporated.aviationweather.common.recycleradapter.GenericViewHolder;
import com.fisincorporated.aviationweather.databinding.CupFileView;

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
