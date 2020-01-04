package org.soaringforecast.rasp.task;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.soaringforecast.rasp.common.CheckBeforeGoingBack;
import org.soaringforecast.rasp.common.Constants;
import org.soaringforecast.rasp.common.MasterActivity;
import org.soaringforecast.rasp.task.edit.EditTaskFragment;
import org.soaringforecast.rasp.task.list.TaskListFragment;
import org.soaringforecast.rasp.turnpoints.messages.AddTurnpointsToTask;
import org.soaringforecast.rasp.task.messages.EditTask;
import org.soaringforecast.rasp.task.messages.SelectedTask;
import org.soaringforecast.rasp.turnpoints.search.TurnpointSearchForTaskFragment;

import java.util.List;


public class TaskActivity extends MasterActivity {

    private static final String TURNPOINT_IMPORT = "TURNPOINT_IMPORT";
    private static final String CREATE_TASK = "CREATE_TASK";
    private static final String EDIT_TASK = "EDIT_TASK";
    private static final String TURNPOINT_OP = "TURNPOINT_OP";
    private static final String TURNPOINT_SEARCH = "TURNPOINT_SEARCH";
    private static final String LIST_TASKS = "LIST_TASKS";
    private static final String ENABLE_CLICK_TASK = "ENABLE_CLICK_TASK";
    private static final String TURNPOINT_EDIT_SEARCH = "TURNPOINT_EDIT_SEARCH";

    private boolean enableClickTask = false;

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
        super.onBackPressed();
    }

    private Fragment getTaskListFragment() {
        enableClickTask =  getIntent().getExtras().getBoolean(ENABLE_CLICK_TASK);
        return  new TaskListFragment();
    }

    // Bus messages

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EditTask event) {
        displayFragment(new EditTaskFragment().setTaskId( event.getTaskId()), true, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AddTurnpointsToTask event) {
        displayFragment(TurnpointSearchForTaskFragment.newInstance(), true,true);
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

    public static class Builder {

        private Bundle bundle;

        private Builder() {
            bundle = new Bundle();
        }

        public static Builder getBuilder() {
           return new Builder();
        }

        public Builder displayTaskList() {
            bundle.putString(TURNPOINT_OP, LIST_TASKS);
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
