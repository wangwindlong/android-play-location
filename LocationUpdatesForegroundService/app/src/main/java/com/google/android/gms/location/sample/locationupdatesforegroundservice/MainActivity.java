/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.location.sample.locationupdatesforegroundservice;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.Manifest;

import android.content.pm.PackageManager;

import android.net.Uri;

import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.location.sample.locationupdatesforegroundservice.adapter.Comment;
import com.google.android.gms.location.sample.locationupdatesforegroundservice.adapter.CommentsAdapter;
import com.google.android.gms.location.sample.locationupdatesforegroundservice.ui.GridItemDividerDecoration;
import com.google.android.gms.location.sample.locationupdatesforegroundservice.ui.WindowInsets;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.location.sample.locationupdatesforegroundservice.ui.WindowInsets.getStatusbarHeight;

/**
 * The only activity in this sample.
 * <p>
 * Note: for apps running in the background on "O" devices (regardless of the targetSdkVersion),
 * location may be computed less frequently than requested when the app is not in the foreground.
 * Apps that use a foreground service -  which involves displaying a non-dismissable
 * notification -  can bypass the background location limits and request location updates as before.
 * <p>
 * This sample uses a long-running bound and started service for location updates. The service is
 * aware of foreground status of this activity, which is the only bound client in
 * this sample. After requesting location updates, when the activity ceases to be in the foreground,
 * the service promotes itself to a foreground service and continues receiving location updates.
 * When the activity comes back to the foreground, the foreground service stops, and the
 * notification associated with that foreground service is removed.
 * <p>
 * While the foreground service notification is displayed, the user has the option to launch the
 * activity from the notification. The user can also remove location updates directly from the
 * notification. This dismisses the notification and stops the service.
 */
public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    // UI elements.
    private Button mRequestLocationUpdatesButton;
    private Button mRemoveLocationUpdatesButton;

    private View fab;
    private DrawerLayout drawer;
    private RecyclerView grid;
    private CommentsAdapter adapter;
    private Toolbar toolbar;
    private GridLayoutManager layoutManager;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    private RecyclerView.OnScrollListener toolbarElevation = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            // we want the grid to scroll over the top of the toolbar but for the toolbar items
            // to be clickable when visible. To achieve this we play games with elevation. The
            // toolbar is laid out in front of the grid but when we scroll, we lower it's elevation
            // to allow the content to pass in front (and reset when scrolled to top of the grid)
            if (newState == RecyclerView.SCROLL_STATE_IDLE
                    && layoutManager.findFirstVisibleItemPosition() == 0
                    && layoutManager.findViewByPosition(0).getTop() == grid.getPaddingTop()
                    && ViewCompat.getTranslationZ(toolbar) != 0) {
                // at top, reset elevation
//                toolbar.setTranslationZ(0f);
                ViewCompat.setTranslationZ(toolbar, 0);
            } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING
                    && ViewCompat.getTranslationZ(toolbar) != -1f) {
                // grid scrolled, lower toolbar to allow content to pass in front
                ViewCompat.setTranslationZ(toolbar, -1f);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myReceiver = new MyReceiver();
        setContentView(R.layout.activity_main);
        fab = findViewById(R.id.fab);
        drawer = findViewById(R.id.drawer);
        drawer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        // Check that the user hasn't revoked permissions by going to Settings.
        if (Utils.requestingLocationUpdates(this)) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }

        grid = findViewById(R.id.grid);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        adapter = new CommentsAdapter(null, 0, 0);
        grid.setAdapter(adapter);

        layoutManager = new GridLayoutManager(this, 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 2;
            }
        });
        grid.setLayoutManager(layoutManager);
        grid.addOnScrollListener(toolbarElevation);
//        grid.addOnScrollListener(new InfiniteScrollListener(layoutManager, dataManager) {
//            @Override
//            public void onLoadMore() {
//                dataManager.loadAllDataSources();
//            }
//        });
        grid.setHasFixedSize(true);
        grid.addItemDecoration(new GridItemDividerDecoration(this, R.dimen.divider_height,
                R.color.divider));
//        grid.setItemAnimator(new HomeGridItemAnimator());

//        RecyclerViewPreloader<Shot> shotPreloader =
//                new RecyclerViewPreloader<>(this, adapter, shotPreloadSizeProvider, 4);
//        grid.addOnScrollListener(shotPreloader);

        List<Comment> comments = new ArrayList<>(100);
        for (int i = 0; i < comments.size(); i++) {
            comments.set(i, new Comment(i, ""));
        }
        adapter.addComments(comments);
        initInsets();
    }

    private void initInsets() {
        // drawer layout treats fitsSystemWindows specially so we have to handle insets ourselves
        if (Build.VERSION.SDK_INT < 21) {
            ViewGroup.MarginLayoutParams lpToolbar = (ViewGroup.MarginLayoutParams) toolbar
                    .getLayoutParams();
            lpToolbar.topMargin += getStatusbarHeight();
            lpToolbar.leftMargin += 0;
            lpToolbar.rightMargin += 0;
            toolbar.setLayoutParams(lpToolbar);

            View statusBarBackground = findViewById(R.id.status_bar_background);
            ViewGroup.LayoutParams lpStatus = statusBarBackground.getLayoutParams();
            lpStatus.height = lpToolbar.topMargin;
            statusBarBackground.setLayoutParams(lpStatus);

            // inset the grid top by statusbar+toolbar & the bottom by the navbar (don't clip)
            grid.setPadding(
                    grid.getPaddingLeft(), // landscape
                    lpToolbar.topMargin
                            + ViewUtils.getActionBarSize(MainActivity.this),
                    grid.getPaddingRight(), // landscape
                    grid.getPaddingBottom());
            Log.d(TAG, "lpToolbar.topMargin=" + lpToolbar.topMargin);
        } else {
            ViewCompat.setOnApplyWindowInsetsListener(drawer, new OnApplyWindowInsetsListener() {
                @Override
                public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insetsCompat) {
                    // inset the toolbar down by the status bar height
                    WindowInsets insets = new WindowInsets(insetsCompat);
                    getRightInsets(insetsCompat);
                    return insets.consumeSystemWindowInsets();
                }
            });
        }
    }

    private void getRightInsets(WindowInsetsCompat insetsCompat) {
        WindowInsets insets = new WindowInsets(insetsCompat);
        Log.d(TAG, "getSystemWindowInsetTop=" + insets.getSystemWindowInsetTop());

        ViewGroup.MarginLayoutParams lpToolbar = (ViewGroup.MarginLayoutParams) toolbar
                .getLayoutParams();
        lpToolbar.topMargin += insets.getSystemWindowInsetTop();
        lpToolbar.leftMargin += insets.getSystemWindowInsetLeft();
        lpToolbar.rightMargin += insets.getSystemWindowInsetRight();
        toolbar.setLayoutParams(lpToolbar);

        // inset the grid top by statusbar+toolbar & the bottom by the navbar (don't clip)
        grid.setPadding(
                grid.getPaddingLeft() + insets.getSystemWindowInsetLeft(), // landscape
                insets.getSystemWindowInsetTop()
                        + ViewUtils.getActionBarSize(MainActivity.this),
                grid.getPaddingRight() + insets.getSystemWindowInsetRight(), // landscape
                grid.getPaddingBottom() + insets.getSystemWindowInsetBottom());

        // inset the fab for the navbar
        ViewGroup.MarginLayoutParams lpFab = (ViewGroup.MarginLayoutParams) fab
                .getLayoutParams();
        lpFab.bottomMargin += insets.getSystemWindowInsetBottom(); // portrait
        lpFab.rightMargin += insets.getSystemWindowInsetRight(); // landscape
        fab.setLayoutParams(lpFab);

        View postingStub = findViewById(R.id.stub_posting_progress);
        ViewGroup.MarginLayoutParams lpPosting =
                (ViewGroup.MarginLayoutParams) postingStub.getLayoutParams();
        lpPosting.bottomMargin += insets.getSystemWindowInsetBottom(); // portrait
        lpPosting.rightMargin += insets.getSystemWindowInsetRight(); // landscape
        postingStub.setLayoutParams(lpPosting);

        // we place a background behind the status bar to combine with it's semi-transparent
        // color to get the desired appearance.  Set it's height to the status bar height
        View statusBarBackground = findViewById(R.id.status_bar_background);
        ViewGroup.LayoutParams lpStatus = statusBarBackground.getLayoutParams();
        lpStatus.height = insets.getSystemWindowInsetTop();
        statusBarBackground.setLayoutParams(lpStatus);

        // inset the filters list for the status bar / navbar
        // need to set the padding end for landscape case
//            final boolean ltr = filtersList.getLayoutDirection() == View.LAYOUT_DIRECTION_LTR;
//            filtersList.setPaddingRelative(filtersList.getPaddingStart(),
//                    filtersList.getPaddingTop() + insets.getSystemWindowInsetTop(),
//                    filtersList.getPaddingEnd() + (ltr ? insets.getSystemWindowInsetRight() :
//                            0),
//                    filtersList.getPaddingBottom() + insets.getSystemWindowInsetBottom());

        // clear this listener so insets aren't re-applied
        ViewCompat.setOnApplyWindowInsetsListener(drawer, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        mRequestLocationUpdatesButton = (Button) findViewById(R.id.request_location_updates_button);
        mRemoveLocationUpdatesButton = (Button) findViewById(R.id.remove_location_updates_button);

        mRequestLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    mService.requestLocationUpdates();
                }
            }
        });

        mRemoveLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.removeLocationUpdates();
            }
        });

        // Restore the state of the buttons when the activity (re)launches.
        setButtonsState(Utils.requestingLocationUpdates(this));

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem dribbbleLogin = menu.findItem(R.id.menu_dribbble_login);
//        if (dribbbleLogin != null) {
//            dribbbleLogin.setTitle(dribbblePrefs.isLoggedIn() ?
//                    R.string.dribbble_log_out : R.string.dribbble_login);
//        }
//        final MenuItem designerNewsLogin = menu.findItem(R.id.menu_designer_news_login);
//        if (designerNewsLogin != null) {
//            designerNewsLogin.setTitle(designerNewsPrefs.isLoggedIn() ?
//                    R.string.designer_news_log_out : R.string.designer_news_login);
//        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_filter:
                drawer.openDrawer(GravityCompat.END);
                return true;
//            case R.id.menu_search:
//                View searchMenuView = toolbar.findViewById(R.id.menu_search);
//                Bundle options = ActivityOptions.makeSceneTransitionAnimation(this, searchMenuView,
//                        getString(R.string.transition_search_back)).toBundle();
//                startActivityForResult(new Intent(this, SearchActivity.class), RC_SEARCH, options);
//                return true;
//            case R.id.menu_dribbble_login:
//                if (!dribbblePrefs.isLoggedIn()) {
//                    dribbblePrefs.login(HomeActivity.this);
//                } else {
//                    dribbblePrefs.logout();
//                    // TODO something better than a toast!!
//                    Toast.makeText(getApplicationContext(), R.string.dribbble_logged_out, Toast
//                            .LENGTH_SHORT).show();
//                }
//                return true;
//            case R.id.menu_designer_news_login:
//                if (!designerNewsPrefs.isLoggedIn()) {
//                    startActivity(new Intent(this, DesignerNewsLogin.class));
//                } else {
//                    designerNewsPrefs.logout(HomeActivity.this);
//                    // TODO something better than a toast!!
//                    Toast.makeText(getApplicationContext(), R.string.designer_news_logged_out,
//                            Toast.LENGTH_SHORT).show();
//                }
//                return true;
//            case R.id.menu_about:
//                startActivity(new Intent(HomeActivity.this, AboutActivity.class),
//                        ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.activity_main),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                mService.requestLocationUpdates();
            } else {
                // Permission denied.
                setButtonsState(false);
                Snackbar.make(
                        findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }

    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                Toast.makeText(MainActivity.this, Utils.getLocationText(location),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Update the buttons state depending on whether location updates are being requested.
        if (s.equals(Utils.KEY_REQUESTING_LOCATION_UPDATES)) {
            setButtonsState(sharedPreferences.getBoolean(Utils.KEY_REQUESTING_LOCATION_UPDATES,
                    false));
        }
    }

    private void setButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
            mRequestLocationUpdatesButton.setEnabled(false);
            mRemoveLocationUpdatesButton.setEnabled(true);
        } else {
            mRequestLocationUpdatesButton.setEnabled(true);
            mRemoveLocationUpdatesButton.setEnabled(false);
        }
    }
}
