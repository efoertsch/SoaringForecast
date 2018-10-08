package com.fisincorporated.aviationweather.turnpoints.download;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.common.MasterActivity;
import com.fisincorporated.aviationweather.repository.AppRepository;
import com.fisincorporated.aviationweather.turnpoints.task.TurnpointSearchFragment;

import javax.inject.Inject;


public class TurnpointsActivity extends MasterActivity {

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
    protected Fragment createFragment() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return null;
        }
        switch (bundle.getString(TURNPOINT_OP)) {
            case TURNPOINT_IMPORT:
                setActivityTitle(R.string.import_turnpoints);
                return getTurnpointImportFragment();
            case CREATE_TASK:
                setActivityTitle(getString(R.string.create_task));
                return getCreateTaskFragment();
            case EDIT_TASK:
                setActivityTitle(getString(R.string.edit_task));
                return getEditTaskFragment();
            case TURNPOINT_SEARCH:
                setActivityTitle(getString(R.string.turnpoint_search));
                return getTurnpointSearchFragment();
                default:
                    return null;
        }

    }

    private Fragment getTurnpointSearchFragment() {
        return TurnpointSearchFragment.newInstance(appRepository);
    }

    private Fragment getTurnpointImportFragment() {
        return new TurnpointsImportFragment();
    }

    //TODO
    private Fragment getEditTaskFragment() {
        return null;
    }

    //TODO
    private Fragment getCreateTaskFragment() {
        return null;
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

        public Builder displayTurnpointSearch(){
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
            Intent intent = new Intent(context, TurnpointsActivity.class);
            intent.putExtras(bundle);
            return intent;
        }
    }

}
