package com.fisincorporated.soaringforecast.task;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.fisincorporated.soaringforecast.common.Constants;
import com.fisincorporated.soaringforecast.common.MasterActivity;
import com.fisincorporated.soaringforecast.messages.AddTurnpointsToTask;
import com.fisincorporated.soaringforecast.messages.EditTask;
import com.fisincorporated.soaringforecast.messages.GoToDownloadImport;
import com.fisincorporated.soaringforecast.messages.GoToTurnpointImport;
import com.fisincorporated.soaringforecast.messages.SelectedTask;
import com.fisincorporated.soaringforecast.task.edit.EditTaskFragment;
import com.fisincorporated.soaringforecast.task.list.TaskListFragment;
import com.fisincorporated.soaringforecast.task.search.TurnpointSearchFragment;
import com.fisincorporated.soaringforecast.task.turnpoints.download.TurnpointsDownloadFragment;
import com.fisincorporated.soaringforecast.task.turnpoints.seeyou.SeeYouImportFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class TaskActivity extends MasterActivity {

    private static final String TURNPOINT_IMPORT = "TURNPOINT_IMPORT";
    private static final String CREATE_TASK = "CREATE_TASK";
    private static final String EDIT_TASK = "EDIT_TASK";
    private static final String TURNPOINT_OP = "TURNPOINT_OP";
    private static final String TURNPOINT_SEARCH = "TURNPOINT_SEARCH";
    private static final String LIST_TASKS = "LIST_TASKS";
    private static final String TASK_NAME = "TASK_NAME";
    private static final String TASK_ID = "TASK_ID";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Fragment createFragment() {
        String turnpointOp = getIntent().getExtras().getString(TURNPOINT_OP);
        if (turnpointOp != null) {
            switch (turnpointOp) {
                case LIST_TASKS:
                    return getTaskListFragment();
                case TURNPOINT_IMPORT:
                    return getSeeYouImportFragment();
                default:
                    return getTaskListFragment();
            }
        }
        return getTaskListFragment();
    }

    private Fragment getSeeYouImportFragment() {
        return SeeYouImportFragment.newInstance();
    }

    private Fragment getTurnpointsDownloadFragment() {
        return new TurnpointsDownloadFragment();
    }

    private Fragment getTaskListFragment() {
        return  new TaskListFragment();
    }

    // Bus messages

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EditTask event) {
        replaceWithThisFragment(new EditTaskFragment().setTaskId( event.getTaskId()), true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AddTurnpointsToTask event) {
        replaceWithThisFragment(new TurnpointSearchFragment(), true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GoToTurnpointImport event) {
        replaceWithThisFragment(getSeeYouImportFragment(),true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GoToDownloadImport event) {
        replaceWithThisFragment(getTurnpointsDownloadFragment(), true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SelectedTask selectedTask){
        Intent intent = getIntent();
        Bundle bundle = new Bundle();
        bundle.putLong(Constants.SELECTED_TASK, selectedTask.getTaskId());
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    public static class Builder {

        private Bundle bundle;

        private Builder() {
            bundle = new Bundle();
        }

        public static Builder getBuilder() {
            Builder builder = new Builder();
            return builder;
        }

        public Builder displayTaskList() {
            bundle.putString(TURNPOINT_OP, LIST_TASKS);
            return this;
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
