package com.fisincorporated.soaringforecast.windy;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
    @Named("appWindyUrl")
    String appWindyUrl;

    @Inject
    AppPreferences appPreferences;

    @Inject
    AppRepository appRepository;

    private WindyViewModel windyViewModel;
    private WindyView windyView;
    private WebView webView;
    private MenuItem clearTaskMenuItem;
    private boolean showClearTaskMenuItem;
    private int webViewHeight = 500;  // set as default

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
                if (Uri.parse(url).getHost().equals(appWindyUrl)) {
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
                webView.loadUrl(appWindyUrl);
            }
        });
        windyViewModel.getCommand().observe(this, command -> {
            // execute javascript command
            executeJavaScriptCommand(command);
        });

        windyViewModel.getTaskSelected().observe(this, isTaskSelected -> {
            displayTaskClearMenuItem(isTaskSelected);
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

}
