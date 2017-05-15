package com.testapp.testtask;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.testapp.testtask.data.CountriesFragment;
import com.testapp.testtask.data.DownloadFragment;
import com.testapp.testtask.data.Territory;

import java.util.Stack;

import static com.testapp.testtask.data.DownloadingService.cancelled;


public class CountriesActivity extends AppCompatActivity {


    private static final String TAG = "CountriesActivity";
    private Territory mTerritory;
    private Stack<String> activityName;

    private Dialog mProgressDialog;
    private TextView mPercentage;
    private ImageView mDownloaded;
    private ImageView mLeftToLoad;
    private ConstraintLayout mDownloadLayout;
    private ProgressBar mProgressBar;
    private ImageView mCancelButton;
    private ImageView mDownloadButton;
    private TextView mProgressInMb;
    private ProgressBar mDialogProgressBar;
    private Button dismissButton;
    private ImageView mDialogCancel;
    private ImageView mMapIcon;

    private BroadcastReceiver mResultsReceiver;

    private CountriesFragment countriesFrag;

    private int width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countries);
        mTerritory = (Territory) getIntent().getSerializableExtra("Continent");
        activityName = new Stack<>();
        activityName.add(mTerritory.getName());
        setTitle(activityName.peek());

        CountriesFragment countriesFragment = new CountriesFragment();

        final FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction().add(R.id.countries_list, countriesFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();
        countriesFrag = (CountriesFragment) fragmentManager.findFragmentById(R.id.countries_list);

        mResultsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("download.initialize")) {
                    showDownloadingScreen(intent);
                }
                else if (intent.getAction().equals("update.data")) {
                    int progress = intent.getIntExtra("progress", -1);
                    // Percentage of completion.
                    String update = String.valueOf(progress) +"%";
                    // Downloaded megabytes.
                    String dialogMagabytes = String.valueOf(intent.getLongExtra("downloadedInMb", -1))
                            + " Mb of " + String.valueOf(intent.getIntExtra("fileLengthMb", -1)) + "Mb";
                    populateRecyclerView(intent.getStringExtra("RegionName"));
                    // Update the views.
                    if (mProgressInMb == null) {
                        showDownloadingScreen(intent);
                    }
                    mProgressInMb.setText(dialogMagabytes);
                    if (mProgressBar != null) {
                        mProgressBar.setProgress(progress);
                    }

                    mDialogProgressBar.setProgress(progress);
                    mPercentage.setText(update);
                    mDownloaded.getLayoutParams().width = (int) (width*((float)progress/(float)100));
                    mLeftToLoad.getLayoutParams().width = (int) (width - (width*(float)progress/(float)100));

                    if (intent.getIntExtra("progress", -1) == 100) {
                        Toast.makeText(context, "Download finished", Toast.LENGTH_SHORT).show();
                        mDownloadLayout.setVisibility(View.GONE);

                        mProgressDialog.dismiss();
                        if (mProgressBar != null && mMapIcon != null && mDownloadButton != null
                                && mCancelButton != null) {
                            mCancelButton.setVisibility(View.GONE);
                            mProgressBar.setVisibility(View.GONE);
                            // Make the icon green.
                            mMapIcon.getDrawable()
                                    .setColorFilter(context.getResources()
                                                    .getColor(R.color.colorIconDownloaded),
                                            PorterDuff.Mode.SRC_IN);
                            mDownloadButton.setVisibility(View.GONE);
                        }
                    }

                }
                else if (intent.getAction().equals("download.cancel")) {
                    mDownloadLayout.setVisibility(View.GONE);
                    if (mProgressBar != null && mCancelButton != null && mDownloadButton != null) {
                        mProgressBar.setVisibility(View.GONE);
                        mCancelButton.setVisibility(View.GONE);
                        mDownloadButton.setVisibility(View.VISIBLE);
                    }
                    mProgressDialog.dismiss();
                    Log.e("DownloadingTask", "Cancelling download in onCancelled");
                    cancelled = false;
                }
            }
        };

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();

        }
        else {
            super.onBackPressed();
            activityName.pop();
            setTitle(activityName.peek());
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "OnPause");

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mResultsReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceivers();
        Log.e(TAG, "OnResume");

    }

    private void registerReceivers() {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mResultsReceiver, new IntentFilter("download.initialize"));
        manager.registerReceiver(mResultsReceiver, new IntentFilter("download.cancel"));
        manager.registerReceiver(mResultsReceiver, new IntentFilter("update.data"));
    }


    public void addActivityName(String newName) {
        activityName.add(newName);
    }

    public void initDownloadScreen(String regionName) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DownloadFragment downloadinFragment = new DownloadFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.downloading_screen, downloadinFragment)
                .commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();


        DownloadFragment currDownloadFragment = (DownloadFragment) fragmentManager
                .findFragmentById(R.id.downloading_screen);

        mPercentage = currDownloadFragment.percentage;
        mDownloaded = currDownloadFragment.downloaded;
        mLeftToLoad = currDownloadFragment.leftToLoad;
        mDownloadLayout = currDownloadFragment.mLayout;
        TextView label = currDownloadFragment.label;
        String labelText = "Downloading " + regionName;
        label.setText(labelText);
    }

    public void showDownloadingScreen(Intent intent) {

        String regionName = intent.getStringExtra("RegionName");
        Log.e(TAG, regionName);
        initDownloadScreen(regionName);
        populateRecyclerView(regionName);

        // Creates custom dialog window with the info about the download.
        mProgressDialog = new Dialog(mDownloadLayout.getContext());
        mProgressDialog.setContentView(R.layout.my_progressdialog);
        // Get the references to Views in progress dialog
        mProgressInMb = (TextView) mProgressDialog.findViewById(R.id.textView_progress_in_mb);
        mDialogCancel = (ImageView) mProgressDialog.findViewById(R.id.imageView_remove_download);
        mDialogProgressBar = (ProgressBar) mProgressDialog.findViewById(R.id.progressBar);
        dismissButton = (Button) mProgressDialog.findViewById(R.id.button_cancel);
        mDialogCancel.setColorFilter(R.color.colorIcons);

        TextView cityLabel = (TextView) mProgressDialog.findViewById(R.id.city_label);
        cityLabel.setText(regionName);
        // Set the width of progress dialog to fill the screen.
        mProgressDialog.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);
        // Set listener for "CANCEL" button.
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressDialog.dismiss();
            }
        });
        // Set listener for X button.
        mDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelled = true;
            }
        });
        // Set listener on the whole downloading layout to show the dialog.
        mDownloadLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressDialog.show();
            }
        });

        // Convert margin into dp
        Resources resources = this.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = 48 * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        // width - margin
        width = (int) (getDisplayWidth() - px);
    }

    public void populateRecyclerView(String name) {
        countriesFrag = (CountriesFragment)
                getSupportFragmentManager().findFragmentById(R.id.countries_list);

        RecyclerView layoutManager = countriesFrag.mRecyclerView;

        for (int i = 0; i < layoutManager.getChildCount(); i++) {
            ConstraintLayout constraintLayout = (ConstraintLayout) layoutManager.getChildAt(i);
            TextView countryName;
            if (constraintLayout.findViewById(R.id.textView_country_name) != null) {
                countryName = (TextView) constraintLayout.findViewById(R.id.textView_country_name);
                if (countryName.getText().toString().equals(name)) {
                    mProgressBar = (ProgressBar) constraintLayout.findViewById(R.id.download_progressbar);
                    mCancelButton = (ImageView) constraintLayout.findViewById(R.id.imageView_cancel_icon);
                    mDownloadButton = (ImageView) constraintLayout.findViewById(R.id.imageView_download_icon);
                    mMapIcon = (ImageView) constraintLayout.findViewById(R.id.imageView_map_icon);

                    mDownloadButton.setVisibility(View.GONE);

                    mCancelButton.setVisibility(View.VISIBLE);
                    mCancelButton.setColorFilter(R.color.colorIcons);

                    mCancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            cancelled = true;
                        }
                    });
                    // Show progress bar underneath the region title.
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            }
        }
    }


    /**
     * Determines the width of a given display.
     * @return the width of the display in px.
     */
    public int getDisplayWidth() {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }
}
