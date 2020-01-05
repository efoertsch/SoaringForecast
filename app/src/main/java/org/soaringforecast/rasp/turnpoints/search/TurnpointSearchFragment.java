package org.soaringforecast.rasp.turnpoints.search;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.turnpoints.list.TurnpointListFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;

public class TurnpointSearchFragment extends TurnpointListFragment {

    protected SearchView searchView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showSearchIconInMenu(false);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.turnpoint_search_menu_item, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(getListener());
        searchView.setQueryHint(getString(R.string.search_for_turnpoints_hint));
        searchView.setIconifiedByDefault(false);
        item.expandActionView();
        searchView.requestFocus();
        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                getActivity().getSupportFragmentManager().popBackStack();
                return true;
            }
        });

    }

    @NonNull
    private SearchView.OnQueryTextListener getListener() {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String txt) {
                // do nothing
                return false;
            }

            @Override
            public boolean onQueryTextChange(String search) {
                if (search.length() == 0) {
                    runSearch(search);
                } else if (search.length() <= 2) {
                    clearTurnpointList();
                } else {
                    runSearch(search);
                }

                return false;
            }
        };
    }

    private void clearTurnpointList() {
        turnpointListAdapter.setTurnpointList(null);
    }

    private void runSearch(String search) {
        turnpointListViewModel.searchTurnpoints(search).observe(this, turnpoints -> turnpointListAdapter.setTurnpointList(turnpoints));
    }

//    private void returnToPreviousScreen() {
//        getActivity().finish();
//        //EventBus.getDefault().post(new PopThisFragmentFromBackStack());
//    }

}
