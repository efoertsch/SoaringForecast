package com.fisincorporated.aviationweather.task.download;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.common.recycleradapter.GenericRecyclerViewAdapter;
import com.fisincorporated.aviationweather.databinding.CupFileView;
import com.fisincorporated.aviationweather.messages.ImportFile;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;

public class TurnpointsImportRecyclerViewAdapter extends GenericRecyclerViewAdapter<File, TurnpointsImportViewHolder> {

    public TurnpointsImportRecyclerViewAdapter(List<File> items){
        super(items);
    }

    @Override
    public TurnpointsImportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CupFileView binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.turnpoint_import_cup_file, parent, false);
        return new TurnpointsImportViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(TurnpointsImportViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        //TODO get better way to do following
        holder.getViewDataBinding().setFileClickListener(this);
    }

    public void updateFileList(List<File> files){
        getItems().clear();
        getItems().addAll(files);
        notifyDataSetChanged();
    }


    public void onFileClick(File file, Integer position){
        EventBus.getDefault().post(new ImportFile(file));

    }



}
