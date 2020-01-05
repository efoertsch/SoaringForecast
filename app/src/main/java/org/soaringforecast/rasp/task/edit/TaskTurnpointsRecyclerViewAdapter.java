package org.soaringforecast.rasp.task.edit;

import androidx.databinding.DataBindingUtil;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.recycleradapter.GenericListClickListener;
import org.soaringforecast.rasp.common.recycleradapter.GenericRecyclerViewAdapter;
import org.soaringforecast.rasp.common.recycleradapter.GenericViewHolder;
import org.soaringforecast.rasp.databinding.TaskTurnpointView;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.TaskTurnpoint;
import org.soaringforecast.rasp.repository.messages.DataBaseError;
import org.soaringforecast.rasp.soaring.forecast.TurnpointBitmapUtils;
import org.soaringforecast.rasp.touchhelper.ItemTouchHelperAdapter;
import org.soaringforecast.rasp.touchhelper.ItemTouchHelperViewHolder;

import java.util.Collections;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TaskTurnpointsRecyclerViewAdapter
        extends GenericRecyclerViewAdapter<TaskTurnpoint, TaskTurnpointsRecyclerViewAdapter.TaskTurnpointViewHolder>
        implements ItemTouchHelperAdapter, DragOps {

    private GenericListClickListener<TaskTurnpoint> itemClickListener;
    private TaskAndTurnpointsViewModel taskAndTurnpointsViewModel;
    private TurnpointBitmapUtils turnpointBitmapUtils;
    private AppRepository appRepository;


    private TaskTurnpointsRecyclerViewAdapter(){}

    public static TaskTurnpointsRecyclerViewAdapter getInstance(){
        return new TaskTurnpointsRecyclerViewAdapter();
    }

    public TaskTurnpointsRecyclerViewAdapter setTaskAndTurnpointViewModel(TaskAndTurnpointsViewModel taskAndTurnpointsViewModel) {
        setItems(taskAndTurnpointsViewModel.getTaskTurnpoints().getValue());
        this.taskAndTurnpointsViewModel = taskAndTurnpointsViewModel;
        return this;
    }

    public TaskTurnpointsRecyclerViewAdapter setItemClickListener(GenericListClickListener<TaskTurnpoint> taskTurnpointGenericListClickListener) {
        itemClickListener = taskTurnpointGenericListClickListener;
        return this;
    }

    public TaskTurnpointsRecyclerViewAdapter setTurnpointBitmapUtils(TurnpointBitmapUtils turnpointBitmapUtils){
        this.turnpointBitmapUtils = turnpointBitmapUtils;
        return this;
    }

    public TaskTurnpointsRecyclerViewAdapter setAppRepository(AppRepository appRepository){
        this.appRepository = appRepository;
        return this;
    }

    @Override
    public TaskTurnpointViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TaskTurnpointView binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.task_turnpoint_detail, parent, false);
        return new TaskTurnpointViewHolder(binding, this);
    }

    @Override
    public void onBindViewHolder(TaskTurnpointViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

    @Override
    public void onViewRecycled(TaskTurnpointsRecyclerViewAdapter.TaskTurnpointViewHolder taskTurnpointViewHolder){
        taskTurnpointViewHolder.dispose();

    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(getItems(), fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        taskAndTurnpointsViewModel.renumberTurnpoints();
        return true;
    }

    @Override
    public void dragCompleted() {
        final Runnable r = new Runnable() {
            public void run() {
                notifyDataSetChanged();
            }
        };
        new Handler().post(r);

    }


    @Override
    public void onItemDismiss(int position) {
        // Need to keep track of deleted turnpoints so can delete from db when(if) user saves changes
        taskAndTurnpointsViewModel.deleteTaskTurnpoint(position);
        notifyDataSetChanged();
    }


    public class TaskTurnpointViewHolder extends GenericViewHolder<TaskTurnpoint, TaskTurnpointView>
            implements ItemTouchHelperViewHolder {

        private TaskTurnpointView viewDataBinding;
        private DragOps dragOps;
        private Disposable disposable;

        TaskTurnpointViewHolder(TaskTurnpointView bindingView, DragOps dragOps) {
            super(bindingView);
            this.dragOps = dragOps;
            viewDataBinding = bindingView;
        }

        public void onBind(TaskTurnpoint item, int position) {
            viewDataBinding.setTaskTurnpoint(item);
            viewDataBinding.setPosition(position);
            viewDataBinding.setClickListener(itemClickListener);
            displayTurnpointIcon(item);

        }

        @Override
        public TaskTurnpointView getViewDataBinding() {
            return viewDataBinding;
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(itemView.getResources().getColor(R.color.drag_color));
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(itemView.getResources().getColor(R.color.drag_drop));
            dragOps.dragCompleted();
        }

        private void displayTurnpointIcon(TaskTurnpoint taskTurnpoint) {
            disposable = appRepository.getTurnpoint(taskTurnpoint.getTitle(), taskTurnpoint.getCode())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(turnpoint-> {
                        viewDataBinding.taskTurnpointIcon.setImageDrawable(
                                turnpointBitmapUtils.getDrawableTurnpointImage(viewDataBinding.getRoot().getContext(), turnpoint));
                            },
                            t -> {
                                EventBus.getDefault().post(new DataBaseError(viewDataBinding.getRoot().getContext()
                                        .getString(R.string.error_reading_turnpoint, taskTurnpoint.getTitle(),
                                        taskTurnpoint.getCode()), t));
                            });
        }

        void dispose(){
            disposable.dispose();
        }

    }

}
