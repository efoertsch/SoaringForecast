package org.soaringforecast.rasp.turnpoints.search;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.common.recycleradapter.GenericListClickListener;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.turnpoints.messages.EditTurnpoint;

public class TurnpointSearchForEditFragment extends TurnpointSearchFragment {

    public static TurnpointSearchForEditFragment newInstance() {
        return new TurnpointSearchForEditFragment();
    }

    private GenericListClickListener<Turnpoint> turnpointTextClickListener = (turnpoint, position) -> {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        searchView.setQuery("", true);
        EventBus.getDefault().post(new EditTurnpoint(turnpoint));
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = super.onCreateView(inflater,container, savedInstanceState);
        turnpointListAdapter.setOnItemClickListener(turnpointTextClickListener);
        return view;
    }


}
