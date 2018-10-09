package com.fisincorporated.aviationweather.task;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.common.MasterActivity;
import com.fisincorporated.aviationweather.messages.AddNewTask;
import com.fisincorporated.aviationweather.messages.AddNewTaskRefused;
import com.fisincorporated.aviationweather.messages.AddTurnpointsToTask;
import com.fisincorporated.aviationweather.messages.AddTurnpointsToTaskRefused;
import com.fisincorporated.aviationweather.repository.AppRepository;
import com.fisincorporated.aviationweather.task.download.TurnpointsImportFragment;
import com.fisincorporated.aviationweather.task.edit.EditTaskFragment;
import com.fisincorporated.aviationweather.task.list.TaskListFragment;
import com.fisincorporated.aviationweather.task.search.TurnpointSearchFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;


public class TaskActivity extends MasterActivity {

    private static final String TURNPOINT_IMPORT = "TURNPOINT_IMPORT";
    private static final String CREATE_TASK = "CREATE_TASK";
    private static final String EDIT_TASK = "EDIT_TASK";
    private static final String TURNPOINT_OP = "TURNPOINT_OP";
    private static final String TURNPOINT_SEARCH = "TURNPOINT_SEARCH";
    private static final String TASK_NAME = "TASK_NAME";

    @Inject
    AppRepository appRepository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart(){
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop(){
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected Fragment createFragment() {
        setActivityTitle(getString(R.string.task_list));
        return TaskListFragment.newInstance(appRepository);
    }


    private Fragment getTurnpointSearchFragment() {
        setActivityTitle(getString(R.string.turnpoint_search));
        return TurnpointSearchFragment.newInstance(appRepository);
    }

    private Fragment getTurnpointImportFragment() {
        setActivityTitle(R.string.import_turnpoints);
        return new TurnpointsImportFragment();
    }

    //TODO
    private Fragment getEditTaskFragment(long taskId) {
        setActivityTitle(getString(R.string.edit_task));
        return EditTaskFragment.newInstance(appRepository, taskId);
    }

    // Bus messages
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AddNewTaskRefused event) {
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AddNewTask event) {
        setActivityTitle(getString(R.string.create_task));
        displayFragment(EditTaskFragment.newInstance(appRepository, event.getTaskId()), true);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AddTurnpointsToTask event) {
        // TODO display turnpoint search to add turnpoints
        event.getTaskId();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AddTurnpointsToTaskRefused event) {
        // TODO go back to task list
    }

    //TODO add menu for other options



    public static class Builder {

        private Bundle bundle;

        private Builder() {
            bundle = new Bundle();
        }

        public static Builder getBuilder() {
            Builder builder = new Builder();
            return builder;
        }

        public Builder displayTurnpointSearch() {
            bundle.putString(TURNPOINT_OP, TURNPOINT_SEARCH);
            return this;
        }

        public Builder displayTurnpointImport() {
            bundle.putString(TURNPOINT_OP, TURNPOINT_IMPORT);
            return this;
        }

        public Builder displayCreateTask() {
            bundle.putString(TURNPOINT_OP, CREATE_TASK);
            return this;
        }

        public Builder displayEditTask() {
            bundle.putString(TURNPOINT_OP, EDIT_TASK);
            return this;
        }

        public Builder editTask(String taskName) {
            bundle.putString(TASK_NAME, taskName);
            return this;
        }

        public Intent build(Context context) {
            Intent intent = new Intent(context, TaskActivity.class);
            intent.putExtras(bundle);
            return intent;
        }
    }

}
