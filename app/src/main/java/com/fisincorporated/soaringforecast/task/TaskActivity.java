package com.fisincorporated.soaringforecast.task;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.fisincorporated.soaringforecast.common.Constants;
import com.fisincorporated.soaringforecast.common.MasterActivity;
import com.fisincorporated.soaringforecast.messages.AddNewTaskRefused;
import com.fisincorporated.soaringforecast.messages.AddTurnpointsToTask;
import com.fisincorporated.soaringforecast.messages.AddTurnpointsToTaskRefused;
import com.fisincorporated.soaringforecast.messages.EditTask;
import com.fisincorporated.soaringforecast.messages.GoToDownloadImport;
import com.fisincorporated.soaringforecast.messages.GoToTurnpointImport;
import com.fisincorporated.soaringforecast.messages.PopThisFragmentFromBackStack;
import com.fisincorporated.soaringforecast.messages.SelectedTask;
import com.fisincorporated.soaringforecast.messages.SnackbarMessage;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.task.edit.EditTaskFragment;
import com.fisincorporated.soaringforecast.task.list.TaskListFragment;
import com.fisincorporated.soaringforecast.task.search.TurnpointSearchFragment;
import com.fisincorporated.soaringforecast.task.turnpoints.download.TurnpointsDownloadFragment;
import com.fisincorporated.soaringforecast.task.turnpoints.seeyou.SeeYouImportFragment;

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
    private static final String LIST_TASKS = "LIST_TASKS";
    private static final String TASK_NAME = "TASK_NAME";
    private static final String TASK_ID = "TASK_ID";

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
        return TurnpointsDownloadFragment.newInstance();
    }

    private Fragment getTaskListFragment() {
        return  TaskListFragment.newInstance(appRepository);
    }


    // Bus messages
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AddNewTaskRefused event) {
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EditTask event) {
        displayFragment(EditTaskFragment.newInstance(appRepository, event.getTaskId()), true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AddTurnpointsToTask event) {
        displayFragment(TurnpointSearchFragment.newInstance(), true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AddTurnpointsToTaskRefused event) {
        popCurrentFragment();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PopThisFragmentFromBackStack event) {
        popCurrentFragment();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GoToTurnpointImport event) {
        displayFragment(getSeeYouImportFragment(),true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GoToDownloadImport event) {
        displayFragment(getTurnpointsDownloadFragment(), true);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SnackbarMessage message) {
        showSnackBarMessage(message.getMessage());
    }

    private void popCurrentFragment() {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack ();
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
