package org.soaringforecast.rasp.task;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.soaringforecast.rasp.common.CheckBeforeGoingBack;
import org.soaringforecast.rasp.common.Constants;
import org.soaringforecast.rasp.common.MasterActivity;
import org.soaringforecast.rasp.task.edit.TaskEditFragment;
import org.soaringforecast.rasp.task.list.TaskListFragment;
import org.soaringforecast.rasp.task.messages.EditTask;
import org.soaringforecast.rasp.task.messages.SelectedTask;
import org.soaringforecast.rasp.turnpoints.airnav.AirNavFragment;
import org.soaringforecast.rasp.turnpoints.messages.AddTurnpointsToTask;
import org.soaringforecast.rasp.turnpoints.messages.DisplayAirNav;
import org.soaringforecast.rasp.turnpoints.messages.GoToTurnpointImport;
import org.soaringforecast.rasp.turnpoints.search.TurnpointSearchForTaskFragment;
import org.soaringforecast.rasp.turnpoints.seeyou.SeeYouImportFragment;

import java.util.List;

import androidx.fragment.app.Fragment;


public class TaskActivity extends MasterActivity {

    private static final String TASK_OP = "TASK_OP";
    private static final String LIST_TASKS = "LIST_TASKS";
    private static final String EDIT_TASK = "EDIT_TASK";
    private static final String TASK_ID = "TASK_ID";
    private static final String ENABLE_CLICK_TASK = "ENABLE_CLICK_TASK";

    private boolean enableClickTask = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Fragment createFragment() {
        String taskOp = getIntent().getExtras().getString(TASK_OP);
        if (taskOp != null) {
            switch (taskOp) {
                case LIST_TASKS:
                    return getTaskListFragment();
                case EDIT_TASK:
                    return getTaskEditFragment();
                default:
                    return getTaskListFragment();
            }
        }
        return getTaskListFragment();
    }


    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            if (fragments.get(fragments.size()-1) instanceof CheckBeforeGoingBack){
                CheckBeforeGoingBack checkBeforeGoingBack = (CheckBeforeGoingBack) fragments.get(fragments.size()-1);
                if (!checkBeforeGoingBack.okToGoBack()){
                   return;
                }
            }
        }
        popCurrentFragment();
    }

    private Fragment getTaskListFragment() {
        enableClickTask =  getIntent().getExtras().getBoolean(ENABLE_CLICK_TASK);
        return  new TaskListFragment();
    }

    private Fragment getTaskEditFragment() {
        return TaskEditFragment.newInstance(getIntent().getExtras().getLong(TASK_ID));
    }


    // Bus messages
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EditTask event) {
        // Doing it this way so when get back to TaskList onResume will fire to update list
        // (in case any change)
        startActivity(Builder.getBuilder().displayTaskEdit(event.getTaskId()).build(this));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AddTurnpointsToTask event) {
        // !!!! Need to keep within same activity so can share same viewmodel data !!!
        displayFragment(TurnpointSearchForTaskFragment.newInstance(), false,true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GoToTurnpointImport event) {
        displayFragment(SeeYouImportFragment.newInstance(), false, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SelectedTask selectedTask){
        if (enableClickTask) {
            Intent intent = getIntent();
            Bundle bundle = new Bundle();
            bundle.putLong(Constants.SELECTED_TASK, selectedTask.getTaskId());
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DisplayAirNav displayAirNav) {
        displayFragment(AirNavFragment.newInstance(displayAirNav.getTurnpoint().getCode()),false,true);
    }

    public static class Builder {

        private Bundle bundle;

        private Builder() {
            bundle = new Bundle();
        }

        public static Builder getBuilder() {
           return new Builder();
        }

        public Builder displayTaskList() {
            bundle.putString(TASK_OP, LIST_TASKS);
            return this;
        }

        public Builder displayTaskEdit(long taskId) {
            bundle.putString(TASK_OP, EDIT_TASK);
            bundle.putLong(TASK_ID, taskId);
            return this;
        }

        public Builder enableClickTask(boolean enableClickTask){
            bundle.putBoolean(ENABLE_CLICK_TASK, enableClickTask);
            return this;
        }

        public Intent build(Context context) {
            Intent intent = new Intent(context, TaskActivity.class);
            intent.putExtras(bundle);
            return intent;
        }
    }

}
