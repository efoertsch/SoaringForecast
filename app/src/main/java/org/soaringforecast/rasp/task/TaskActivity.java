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
import org.soaringforecast.rasp.task.messages.AddTurnpointsToTask;
import org.soaringforecast.rasp.task.messages.EditTask;
import org.soaringforecast.rasp.task.messages.EditTurnpoint;
import org.soaringforecast.rasp.task.messages.GoToDownloadImport;
import org.soaringforecast.rasp.task.messages.GoToTurnpointImport;
import org.soaringforecast.rasp.task.messages.SelectedTask;
import org.soaringforecast.rasp.task.search.TurnpointSearchForEditFragment;
import org.soaringforecast.rasp.task.search.TurnpointSearchForTaskFragment;
import org.soaringforecast.rasp.task.turnpoints.download.TurnpointsDownloadFragment;
import org.soaringforecast.rasp.task.turnpoints.seeyou.SeeYouImportFragment;
import org.soaringforecast.rasp.turnpoint.TurnpointEditFragment;

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
                case TURNPOINT_IMPORT:
                    return getSeeYouImportFragment();
                case TURNPOINT_EDIT_SEARCH:
                    return getTurnpointSearchForEditFragment();
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

    private Fragment getSeeYouImportFragment() {
        return SeeYouImportFragment.newInstance();
    }

    private Fragment getTurnpointSearchForEditFragment(){
        return TurnpointSearchForEditFragment.newInstance();
    }

    private Fragment getTurnpointsDownloadFragment() {
        return new TurnpointsDownloadFragment();
    }

    private Fragment getTaskListFragment() {
        enableClickTask =  getIntent().getExtras().getBoolean(ENABLE_CLICK_TASK);
        return  new TaskListFragment();
    }

    private Fragment getTurnpointEditFragment(long turnpointId) {
        return  TurnpointEditFragment.newInstance(turnpointId);
    }

    // Bus messages

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EditTask event) {
        displayFragment(new EditTaskFragment().setTaskId( event.getTaskId()), true, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AddTurnpointsToTask event) {
        displayFragment(new TurnpointSearchForTaskFragment(), true,true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GoToTurnpointImport event) {
        displayFragment(getSeeYouImportFragment(),true,true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GoToDownloadImport event) {
        displayFragment(getTurnpointsDownloadFragment(), true,true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EditTurnpoint event){
        displayFragment(getTurnpointEditFragment(event.getTurnpointId()), false, true);
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

        public Builder displayTurnpointEditSearch() {
            bundle.putString(TURNPOINT_OP, TURNPOINT_EDIT_SEARCH);
            return this;
        }

        public Builder displayTurnpointImport() {
            bundle.putString(TURNPOINT_OP, TURNPOINT_IMPORT);
            return this;
        }


        public Intent build(Context context) {
            Intent intent = new Intent(context, TaskActivity.class);
            intent.putExtras(bundle);
            return intent;
        }
    }

}
