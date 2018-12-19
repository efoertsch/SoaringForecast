package com.fisincorporated.soaringforecast.windy;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.common.Constants;
import com.fisincorporated.soaringforecast.databinding.WindyView;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.task.TaskActivity;
import com.fisincorporated.soaringforecast.utils.StringUtils;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.android.support.DaggerFragment;

// What has to happen here
// 1. Determine size of webView
// 2. Pass size of webview to html to get proper size for Windy
// 3. Once Windy set up get/draw task if there is one
// 4. Set appropriate menu items

public class WindyFragment extends DaggerFragment {

    @Inject
    @Named("windyHtmlFileName")
    String windyFile;

    @Inject
    AppPreferences appPreferences;

    @Inject
    AppRepository appRepository;

    private WindyViewModel windyViewModel;
    private WindyView windyView;
    private WebView webView;
    private MenuItem clearTaskMenuItem;
    private boolean showClearTaskMenuItem;
    private int lastModelPosition = -1;
    private int lastModelLayerPosition = -1;
    private int lastAltitudePosition = -1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Menus turned off until issues with drawing task on Windy resolved
        setHasOptionsMenu(true);
        windyViewModel = ViewModelProviders.of(this)
                .get(WindyViewModel.class)
                .setAppPreferences(appPreferences)
                .setAppRepository(appRepository);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        windyView = DataBindingUtil.inflate(inflater, R.layout.fragment_windy, container, false);
        windyView.setLifecycleOwner(getActivity());
        windyView.setViewModel(windyViewModel);
        setupViews();
        return windyView.getRoot();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupViews() {
        webView = windyView.fragmentWindyWebview;
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(windyViewModel, "android");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        // prevent popups and new windows (but do not override the onCreateWindow() )
        webView.getSettings().setSupportMultipleWindows(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Uri.parse(url).getHost().equals(windyFile)) {
                    // This is app related url so ignore
                    return false;
                }
                // Otherwise, start browser
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }

            public void onPageFinished(WebView view, String url) {
                // ???
            }
        });

        webView.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ViewTreeObserver viewTreeObserver = webView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    webView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    // now should have webview height
                    setObservers();
                }
            });
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.windy_menu, menu);
        clearTaskMenuItem = menu.findItem(R.id.forecast_menu_clear_task);
        if (clearTaskMenuItem != null) {
            clearTaskMenuItem.setVisible(showClearTaskMenuItem);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.forecast_menu_select_task:
                selectTask();
                return true;
            case R.id.forecast_menu_clear_task:
                windyViewModel.removeTaskTurnpoints();
                displayTaskClearMenuItem(false);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayTaskClearMenuItem(boolean visible) {
        showClearTaskMenuItem = visible;
        getActivity().invalidateOptionsMenu();
    }

    public void setObservers() {
        windyViewModel.getStartUpComplete().observe(this, isComplete -> {
            if (isComplete) {
                //webView.loadUrl(windyFile);
                webView.loadData(getWindyHTML(webView.getHeight())
                             ,"text/html; charset=utf-8", "UTF-8");
            }
        });
        windyViewModel.getCommand().observe(this, command -> {
            // execute javascript command
            executeJavaScriptCommand(command);
        });

        windyViewModel.getTaskSelected().observe(this, isTaskSelected -> {
            displayTaskClearMenuItem(isTaskSelected);
        });

        windyViewModel.getModelPosition().observe(this, position -> {
            if (lastModelPosition != position) {
                lastModelPosition = position;
                windyViewModel.setModelPosition(position);
            }
        });

        windyViewModel.getModelLayerPosition().observe(this, position -> {
            if (lastModelLayerPosition != position) {
                lastModelLayerPosition = position;
                windyViewModel.setModelLayerPosition(position);
            }
        });

        windyViewModel.getAltitudePosition().observe(this, position -> {
            if (lastAltitudePosition != position) {
                lastAltitudePosition = position;
                windyViewModel.setAltitudePosition(position);
            }
        });

    }

    private void executeJavaScriptCommand(String command) {
        webView.evaluateJavascript(command, null);
    }

    private void selectTask() {
        TaskActivity.Builder builder = TaskActivity.Builder.getBuilder();
        builder.displayTaskList().enableClickTask(true);
        startActivityForResult(builder.build(this.getContext()), 999);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle bundle;
        if (requestCode == 999 && data != null) {
            if ((bundle = data.getExtras()) != null) {
                long taskId = bundle.getLong(Constants.SELECTED_TASK);
                if (taskId >= 0) {
                    windyViewModel.setTaskId(taskId);
                }
            }
        }

    }

    static final String REPLACEMENT_HEIGHT_SEARCH = "XXXHEIGHTXXX";

    // Ok a hack, but set the height of windy to the webview height
    private String getWindyHTML(int height) {
        String html = StringUtils.readFromAssetsFolder(getContext(), "windy_test.html"
                , REPLACEMENT_HEIGHT_SEARCH, pxToDp(height - 250) + "px");
        return html;
    }

    //TODO put in utility class
    private int pxToDp(int px) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }
}
