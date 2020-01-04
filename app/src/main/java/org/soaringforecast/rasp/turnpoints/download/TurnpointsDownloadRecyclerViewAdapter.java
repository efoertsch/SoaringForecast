package org.soaringforecast.rasp.turnpoints.download;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.recycleradapter.GenericRecyclerViewAdapter;
import org.soaringforecast.rasp.databinding.CupFileView;
import org.soaringforecast.rasp.turnpoints.messages.ImportFile;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;

public class TurnpointsDownloadRecyclerViewAdapter extends GenericRecyclerViewAdapter<File, TurnpointsDownloadViewHolder> {

    public TurnpointsDownloadRecyclerViewAdapter(List<File> items){
        super(items);
    }

    @Override
    public TurnpointsDownloadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CupFileView binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.turnpoint_download_cup_file, parent, false);
        return new TurnpointsDownloadViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(TurnpointsDownloadViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        //TODO get better way to do following
        holder.getViewDataBinding().setFileClickListener(this);
    }

    public void onFileClick(File file, Integer position){
        EventBus.getDefault().post(new ImportFile(file));

    }



}
